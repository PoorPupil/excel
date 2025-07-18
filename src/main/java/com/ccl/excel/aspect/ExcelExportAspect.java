package com.ccl.excel.aspect;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 优化版 Excel 导出切面：
 * - 采用生产者-消费者模型控制子线程查询和主线程写入节奏
 * - 避免子线程直接持有 SXSSFWorkbook，同步写入集中在单线程
 */
@Slf4j
@Component
@Aspect
public class ExcelExportAspect {

    @Autowired
    private Executor excelExportTaskExecutor;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private ExportTaskLogService exportTaskLogService;

    @Pointcut("@annotation(com.ccl.excel.annotion.ExcelExport)")
    public void excelExportPointcut() {}

    @Around("excelExportPointcut()")
    public Object aroundExcelExport(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Method method = sig.getMethod();
        ExcelExport ann = method.getAnnotation(ExcelExport.class);
        int batchSize = ann.batchSize();
        long timeout = ann.timeoutSeconds();
        String name = ann.exportName();
        String beanName = ann.strategyBeanName();
        String sheetName = ann.sheetName();

        Object[] args = joinPoint.getArgs();
//        @SuppressWarnings("unchecked")
        BatchExportStrategy<Object> strategy = (BatchExportStrategy<Object>)
                applicationContext.getBean(beanName);

        ExportTaskLog taskLog = new ExportTaskLog();
        taskLog.setExportName(name);
        exportTaskLogService.createExportLog(taskLog);
        String taskId = taskLog.getId();

        long total = strategy.getTotalCount(args);
        taskLog.setTotalRecords(total);
        exportTaskLogService.updateExportLogStatus(taskId, ExportStatus.IN_PROGRESS.getValue(), null);

        if (total == 0) {
            exportTaskLogService.finalizeExportLog(taskId, ExportStatus.COMPLETED.getValue(), null, 0L, null);
            return "任务 " + taskId + " 无数据";
        }

        int pages = (int) Math.ceil((double) total / batchSize);
        SXSSFWorkbook workbook = ExcelUtil.createWorkbook();
        SXSSFSheet sheet = workbook.createSheet(sheetName);
        ExcelUtil.writeHeaders(sheet, strategy.getHeaders());

        BlockingQueue<List<Object>> queue = new ArrayBlockingQueue<>(Runtime.getRuntime().availableProcessors() * 2);
        AtomicLong rowsWritten = new AtomicLong();

        for (int i = 0; i < pages; i++) {
            final long offset = (long) i * batchSize;
            excelExportTaskExecutor.execute(() -> {
                try {
                    List<Object> data = strategy.fetchDataSegment(offset, batchSize, args);
                    //  BlockingQueue 添加任务到队列有三种方式：
                    // 1. put(e)：如果队列已满，则阻塞当前线程，直到队列有空间。
                    // 2. offer(e)：如果队列已满，则返回false，不阻塞当前线程。
                    // 3. offer(e,timeout)：如果队列已满，则阻塞当前线程，直到队列有空间或者超时。
                    // 4. add(e)：如果队列已满，则抛出异常。
                    // 这里一定要选择 put
                    queue.put(data);
                } catch (Exception e) {
                    log.error("任务 {} 生产者异常", taskId, e);
                    throw new ExcelExportException(e.getMessage(), e);
                }
            });
        }

        for (int i = 0; i < pages; i++) {
            List<Object> batch = queue.poll(timeout, TimeUnit.SECONDS);
            if (batch == null) {
                log.warn("任务 {} 超时等待数据，已写 {} 行", taskId, rowsWritten.get());
                break;
            }
            int baseRow = (int) (i * batchSize + 1);
            for (int j = 0; j < batch.size(); j++) {
                ExcelUtil.writeRow(sheet, baseRow + j,
                        strategy.convertToRow(batch.get(j)));
                rowsWritten.incrementAndGet();
            }
        }

        String path;
        try (FileOutputStream fos = new FileOutputStream("/tmp/export_" + taskId + ".xlsx")) {
            workbook.write(fos);
            path = "/tmp/export_" + taskId + ".xlsx";
        }
        workbook.dispose();

        exportTaskLogService.finalizeExportLog(taskId, ExportStatus.COMPLETED.getValue(), path, rowsWritten.get(), null);
        return "导出完成，任务ID=" + taskId;
    }
}
