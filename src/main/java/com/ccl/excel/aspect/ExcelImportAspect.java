package com.ccl.excel.aspect;

import com.ccl.excel.annotion.ExcelImport;
import com.ccl.excel.constant.ImportStatus;
//import com.ccl.excel.mapper.ImportRecordRepository;
import com.ccl.excel.pojo.ImportRecord;
import com.ccl.excel.service.impl.ImportRecordServiceImpl;
import com.ccl.excel.strategy.BatchImportStrategy;
import com.ccl.excel.task.ExcelImportBatchTask;
import com.ccl.excel.utils.ExcelReadListener;
import com.ccl.excel.utils.ExcelReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * AOP切面，用于拦截带有@ExcelImport注解的方法，实现Excel自动导入逻辑。
 */
@Slf4j
@Aspect
@Component
public class ExcelImportAspect {

    private final Executor excelImportTaskExecutor;
    private final ApplicationContext applicationContext; // 注入ApplicationContext
    @Resource
    private ImportRecordServiceImpl importRecordServiceImpl;

    // 线程安全的列表，用于收集所有批次导入中产生的失败记录
    // 注意：这个列表是针对整个应用生命周期的，如果并发导入任务很多，
    // 可能会有交叉污染。更好的做法是为每个导入任务维护一个独立的失败记录列表，
    // 例如通过ConcurrentHashMap<String, List<Map<String, String>>>来存储，键为importJobId
    private final ConcurrentHashMap<String, List<Map<String, String>>> allFailedRecordsMap = new ConcurrentHashMap<>();

    public ExcelImportAspect(
            @Qualifier("excelImportTaskExecutor") Executor excelImportTaskExecutor,
                             ApplicationContext applicationContext) {
        this.excelImportTaskExecutor = excelImportTaskExecutor;
        this.applicationContext = applicationContext;
    }

    /**
     * 定义切点，匹配所有带有@ExcelImport注解的方法。
     */
    @Pointcut("@annotation(com.ccl.excel.annotion.ExcelImport)")
    public void excelImportPointcut() {
    }

    /**
     * 环绕通知，实现Excel导入的核心逻辑。
     * @param joinPoint 切点连接点
     * @return 业务方法的返回值
     * @throws Throwable 如果发生异常
     */
    @Around("excelImportPointcut()")
    public Object aroundExcelImport(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ExcelImport excelImportAnnotation = method.getAnnotation(ExcelImport.class);

        // 获取注解参数
        int batchSize = excelImportAnnotation.batchSize();
        long timeoutSeconds = excelImportAnnotation.timeoutSeconds();
        Class<? extends BatchImportStrategy<?>> strategyClass = excelImportAnnotation.strategy();

        // 从Spring容器中获取导入策略的实例
        BatchImportStrategy<Object> importStrategy = (BatchImportStrategy<Object>) applicationContext.getBean(strategyClass);

        // 获取方法参数中的MultipartFile
        MultipartFile excelFile = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof MultipartFile) {
                excelFile = (MultipartFile) arg;
                break;
            }
        }

        if (excelFile == null) {
            throw new IllegalArgumentException("方法参数中未找到MultipartFile类型的Excel文件。");
        }

        // 1. 记录Excel操作记录的开始导入数据
        ImportRecord importRecord = new ImportRecord();
        importRecord.setFileName(excelFile.getOriginalFilename());
        importRecord.setStatus(ImportStatus.STARTED.getValue());
        importRecordServiceImpl.save(importRecord);

        String importJobId = importRecord.getId();
        log.info("导入任务 [" + importJobId + "] 已开始，文件: " + excelFile.getOriginalFilename());

        // 初始化当前任务的失败记录列表
        allFailedRecordsMap.put(importJobId, new CopyOnWriteArrayList<>());

        // 用于收集所有批处理任务的Future
        List<CompletableFuture<List<Map<String, String>>>> futures = new ArrayList<>();

        try (InputStream is = excelFile.getInputStream()) {
            OPCPackage pkg = OPCPackage.open(is);
            XSSFReader xssfReader = new XSSFReader(pkg);
            SharedStrings sst = xssfReader.getSharedStringsTable();

            ExcelReadListener listener = new ExcelReadListener(batchSize, rawBatchData -> {
                // 将原始Map数据转换为目标POJO列表
                List<Object> convertedBatchData = rawBatchData.stream()
                        .map(importStrategy::convertRow)
                        .collect(Collectors.toList());

                // 为每个批次数据创建一个Callable任务
                ExcelImportBatchTask<Object> task = new ExcelImportBatchTask<>(
                        convertedBatchData, importJobId, importStrategy);

                CompletableFuture<List<Map<String, String>>> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(5000);
                        return task.call();
                    } catch (Exception e) {
                        log.error("批处理任务执行异常: " + e.getMessage());
                        // 如果任务本身抛出异常，将整个批次标记为失败
                        // 否则，无法设置错误，直接添加原始Map
                        return (List<Map<String, String>>) new ArrayList<Map<String, String>>(rawBatchData);
                    }
                }, excelImportTaskExecutor);

                future.thenAccept(batchFailed -> {
                    if (batchFailed != null && !batchFailed.isEmpty()) {
                        allFailedRecordsMap.get(importJobId).addAll(batchFailed);
                    }
                });
                futures.add(future);
            });

            listener.process(excelFile.getInputStream(), sst);

        } catch (Exception e) {
            importRecord.setStatus(ImportStatus.FAILED.getValue());
            importRecord.setEndTime(LocalDateTime.now());
            importRecordServiceImpl.updateById(importRecord);
            log.error("Excel文件读取或解析失败: " + e.getMessage());
            throw new RuntimeException("Excel文件读取或解析失败", e);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        allOf.whenComplete((result, ex) -> {
            ImportRecord updateRecord = new ImportRecord();
            updateRecord.setId(importJobId);
            updateRecord.setEndTime(LocalDateTime.now());
            List<Map<String, String>> currentFailedRecords = allFailedRecordsMap.getOrDefault(importJobId, new ArrayList<>(0));

            if (ex != null) {
                updateRecord.setStatus(ImportStatus.FAILED.getValue());
                log.error("导入任务 [" + importJobId + "] 存在子任务异常: " + ex.getMessage());
            } else {
                if (currentFailedRecords.isEmpty()) {
                    updateRecord.setStatus(ImportStatus.COMPLETED_SUCCESS.getValue());
                    log.info("导入任务 [" + importJobId + "] 已全部成功完成。");
                } else {
                    updateRecord.setStatus(ImportStatus.COMPLETED_WITH_ERRORS.getValue());
                    log.info("导入任务 [" + importJobId + "] 已完成，但存在失败记录。");

                    String failedReportFileName = "failed_import_" + importJobId + ".xlsx";
                    String failedReportPath = System.getProperty("java.io.tmpdir") + failedReportFileName;
                    try {
                        // 使用策略获取错误报告的列头
                        ExcelReportGenerator.generateErrorExcel(currentFailedRecords, failedReportPath, importStrategy.getErrorHeaders());
                        updateRecord.setFailedReportPath(failedReportPath);
                    } catch (IOException e) {
                        log.error("生成失败报告Excel失败: " + e.getMessage());
                    }
                }
            }
            importRecordServiceImpl.updateById(updateRecord);
            // 任务完成后，移除该任务的失败记录列表
            allFailedRecordsMap.remove(importJobId);
        });

        try {
            allOf.get(timeoutSeconds, TimeUnit.SECONDS);
            return "Excel导入任务 [" + importJobId + "] 已完成。";
        } catch (TimeoutException e) {
            importRecord.setStatus(ImportStatus.IN_PROGRESS.getValue());
            importRecordServiceImpl.updateById(importRecord);
            log.info("导入任务 [" + importJobId + "] 主线程超时，将在后台继续处理。");
            return "Excel导入任务已提交，正在后台处理中，任务ID: " + importJobId + "。请稍后查询结果。";
        } catch (Exception e) {
            importRecord.setStatus(ImportStatus.FAILED.getValue());
            importRecord.setEndTime(LocalDateTime.now());
            importRecordServiceImpl.updateById(importRecord);
            log.error("导入任务 [" + importJobId + "] 执行过程中发生异常: " + e.getMessage());
            throw new RuntimeException("Excel导入任务执行失败", e);
        }
    }
}