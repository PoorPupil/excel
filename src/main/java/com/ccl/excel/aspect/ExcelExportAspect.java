package com.ccl.excel.aspect;

import cn.hutool.json.JSONUtil;
import com.ccl.excel.annotion.ExcelExport;
import com.ccl.excel.constant.ExportStatus;
import com.ccl.excel.execption.ExcelExportException;
import com.ccl.excel.pojo.ExportTaskLog;
import com.ccl.excel.service.ExportTaskLogService;
import com.ccl.excel.strategy.BatchExportStrategy;
import com.ccl.excel.utils.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileOutputStream; // 示例：实际可能上传到云存储
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Excel 导出切面，用于拦截带有 @ExcelExport 注解的方法，实现异步导出逻辑。
 */
@Slf4j
@Component
@Aspect
public class ExcelExportAspect {

    @Resource(name = "excelExportTaskExecutor") // 注入专门用于 Excel 导出的线程池
    private Executor excelExportTaskExecutor;

    @Resource
    private ApplicationContext applicationContext; // 注入 ApplicationContext 用于获取策略 Bean

    @Resource
    private ExportTaskLogService exportTaskLogService; // 注入导出任务日志服务

    // 用于存储每个导出任务的 SXSSFWorkbook 实例，以便在后台线程中共享和最终写入
    // 注意：这里使用 ConcurrentHashMap 存储，但实际写入时仍需保证对单个 workbook 的串行操作
    // 更好的做法是让主线程创建 workbook 和 sheet，子线程只写入各自的 sheet
    private final ConcurrentHashMap<String, SXSSFWorkbook> workbookMap = new ConcurrentHashMap<>();

    /**
     * 定义切点，匹配所有带有 @ExcelExport 注解的方法。
     */
    @Pointcut("@annotation(com.ccl.excel.annotion.ExcelExport)")
    public void excelExportPointcut() {
    }

    /**
     * 环绕通知，实现 Excel 导出的核心逻辑。
     *
     * @param joinPoint 切点连接点
     * @return 业务方法的返回值 (通常是导出任务ID或状态信息)
     * @throws Throwable 如果发生异常
     */
    @Around("excelExportPointcut()")
    public Object aroundExcelExport(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ExcelExport excelExportAnnotation = method.getAnnotation(ExcelExport.class);

        // 获取注解参数
        int batchSize = excelExportAnnotation.batchSize();
        long timeoutSeconds = excelExportAnnotation.timeoutSeconds();
        String exportName = excelExportAnnotation.exportName();
        String strategyBeanName = excelExportAnnotation.strategyBeanName();

        // 获取原始方法参数
        Object[] methodArgs = joinPoint.getArgs();
        String requestParamsJson = JSONUtil.toJsonStr(methodArgs); // 序列化请求参数

        // 1. 从 Spring 容器中获取导出策略的实例
        BatchExportStrategy<Object> exportStrategy;
        try {
            exportStrategy = (BatchExportStrategy<Object>) applicationContext.getBean(strategyBeanName);
        } catch (Exception e) {
            log.error("无法获取 BatchExportStrategy Bean: {}", strategyBeanName, e);
            throw new ExcelExportException("无法获取指定的导出策略: " + strategyBeanName, e);
        }

        // 2. 创建并保存初始导出任务日志
        ExportTaskLog exportTaskLog = new ExportTaskLog();
        exportTaskLog.setExportName(exportName);
        exportTaskLog.setRequestParams(requestParamsJson);
        // 初始状态为 PENDING，稍后更新为 IN_PROGRESS
        exportTaskLogService.createExportLog(exportTaskLog);
        String taskId = exportTaskLog.getId();

        // 3. 获取总数据量
        long totalCount = exportStrategy.getTotalCount(methodArgs);
        exportTaskLog.setTotalRecords(totalCount);
        exportTaskLogService.updateExportLogStatus(taskId, ExportStatus.IN_PROGRESS.getValue(), null); // 更新状态为进行中

        if (totalCount == 0) {
            log.info("导出任务 {}：没有数据可导出。", taskId);
            exportTaskLogService.finalizeExportLog(taskId, ExportStatus.COMPLETED.getValue(), "无数据导出", 0L, null);
            return "导出任务已提交，任务ID: " + taskId + " (无数据导出)";
        }

        // 4. 计算批次数量
        int numBatches = (int) Math.ceil((double) totalCount / batchSize);
        log.info("导出任务 {}：总记录数={}, 批次大小={}, 总批次={}", taskId, totalCount, batchSize, numBatches);

        // 5. 创建 SXSSFWorkbook 和 Sheets
        SXSSFWorkbook workbook = ExcelUtil.createWorkbook();
        workbookMap.put(taskId, workbook); // 将 workbook 放入 map 供后续访问

        List<String> headers = exportStrategy.getHeaders();
        if (headers == null || headers.isEmpty()) {
            throw new ExcelExportException("导出策略未提供 Excel 表头！");
        }

        // 6. 启动子线程进行数据查询和 Excel 写入
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        AtomicLong totalExportedRows = new AtomicLong(0); // 原子计数器，记录实际导出行数

        // 为每个批次创建单独的 Sheet，或者将所有数据写入一个 Sheet 并由子线程写入不同行范围
        // 这里采用为每个批次创建一个 Sheet 的方式，简化并发写入逻辑
        // 也可以只创建一个 Sheet，但需要子线程协调写入行号，更复杂
        SXSSFSheet mainSheet = workbook.createSheet("导出数据"); // 创建主 Sheet
        ExcelUtil.writeHeaders(mainSheet, headers); // 写入主 Sheet 表头

        for (int i = 0; i < numBatches; i++) {
            long offset = (long) i * batchSize;
            long limit = batchSize;
            int currentBatchIndex = i; // 捕获当前批次索引

            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long rowsWrittenInBatch = 0;
                try {
                    // 获取当前批次对应的 Sheet
                    // 如果所有数据写入一个 Sheet，需要同步写入行号
                    // 这里简化为所有数据写入 mainSheet，并同步控制行号
                    synchronized (mainSheet) { // 确保对 mainSheet 的写入是同步的
                        List<Object> dataSegment = exportStrategy.fetchDataSegment(offset, limit, methodArgs);
                        if (dataSegment != null && !dataSegment.isEmpty()) {
                            int startRow = (int) (offset + 1); // 从第二行开始写入数据 (第一行是表头)
                            for (Object data : dataSegment) {
                                List<String> rowData = exportStrategy.convertToRow(data);
                                ExcelUtil.writeRow(mainSheet, startRow + (int)rowsWrittenInBatch, rowData);
                                rowsWrittenInBatch++;
                            }
                            log.info("导出任务 {}：批次 {} (offset={}, limit={}) 成功导出 {} 行。",
                                    taskId, currentBatchIndex, offset, limit, rowsWrittenInBatch);
                        }
                    }
                    totalExportedRows.addAndGet(rowsWrittenInBatch);
                    return rowsWrittenInBatch;
                } catch (Exception e) {
                    log.error("导出任务 {}：批次 {} (offset={}, limit={}) 导出失败。",
                            taskId, currentBatchIndex, offset, limit, e);
                    // 抛出异常，让 allOfFuture 捕获
                    throw new ExcelExportException("批次导出失败: " + e.getMessage(), e);
                }
            }, excelExportTaskExecutor);
            futures.add(future);
        }

        // 7. 组合所有 CompletableFuture 并设置主线程超时
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            // 主线程等待所有子任务完成，或达到超时时间
            allOfFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            // 如果所有任务在超时时间内完成
            log.info("导出任务 {}：所有子任务在规定时间内完成。", taskId);
            // 最终处理：保存文件并更新日志
            handleExportCompletion(taskId, workbook, totalExportedRows.get(), null);
            return "导出任务已提交，任务ID: " + taskId + "，已完成。";

        } catch (TimeoutException e) {
            log.warn("导出任务 {}：主线程等待超时 ({} 秒)，将返回前端，后台继续执行。", taskId, timeoutSeconds);
            // 主线程超时，更新日志状态并立即返回
            exportTaskLogService.updateExportLogStatus(taskId, ExportStatus.TIMED_OUT.getValue(), null);

            // 注册一个回调，确保在所有子任务完成后，后台仍然执行文件保存和日志更新
            allOfFuture.whenCompleteAsync((result, throwable) -> {
                handleExportCompletion(taskId, workbook, totalExportedRows.get(), throwable);
            }, excelExportTaskExecutor); // 在导出线程池中执行回调

            return "导出任务已提交，任务ID: " + taskId + "，正在后台处理中，请稍后查看导出记录。";

        } catch (Exception e) {
            log.error("导出任务 {}：导出过程中发生异常。", taskId, e);
            // 发生其他异常，更新日志状态为失败
            handleExportCompletion(taskId, workbook, totalExportedRows.get(), e);
            throw new ExcelExportException("Excel 导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理导出任务的最终完成逻辑，包括文件保存和日志更新。
     * 无论主线程是否超时，此方法都会在所有子任务完成后被调用。
     *
     * @param taskId            导出任务ID
     * @param workbook          SXSSFWorkbook 实例
     * @param exportedRecords   实际导出的记录数
     * @param throwable         如果发生异常，则为异常对象
     */
    private void handleExportCompletion(String taskId, SXSSFWorkbook workbook, Long exportedRecords, Throwable throwable) {
        // 从 map 中移除 workbook，防止内存泄露
        workbookMap.remove(taskId);

        String filePath = null;
        Integer finalStatus = ExportStatus.COMPLETED.getValue();
        String errorMessage = null;

        if (throwable != null) {
            finalStatus = ExportStatus.FAILED.getValue();
            errorMessage = "导出失败: " + (throwable.getMessage() != null ? throwable.getMessage() : throwable.toString());
            log.error("导出任务 {} 最终完成时出现错误：{}", taskId, errorMessage);
        } else {
            // 8. 保存 Excel 文件 (示例：保存到本地文件系统)
            String fileName = "export_" + taskId + ".xlsx";
            // 实际应用中，这里应该上传到云存储 (如 S3, OSS) 并获取 URL
            String tempDir = System.getProperty("java.io.tmpdir"); // 获取系统临时目录
            filePath = tempDir + "/" + fileName;

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
                log.info("导出任务 {}：Excel 文件已保存到 {}", taskId, filePath);
            } catch (IOException e) {
                log.error("导出任务 {}：保存 Excel 文件失败。", taskId, e);
                finalStatus = ExportStatus.FAILED.getValue();
                errorMessage = "保存 Excel 文件失败: " + e.getMessage();
            } finally {
                try {
                    workbook.close(); // 关闭 workbook 释放资源
                    workbook.dispose(); // 释放临时文件
                } catch (IOException e) {
                    log.error("导出任务 {}：关闭 workbook 失败。", taskId, e);
                }
            }
        }

        // 9. 更新导出任务日志
        exportTaskLogService.finalizeExportLog(taskId, finalStatus, filePath, exportedRecords, errorMessage);
    }
}