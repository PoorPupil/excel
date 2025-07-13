package com.ccl.excel.task;

import com.ccl.excel.strategy.BatchImportStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Excel导入的批处理任务，实现Callable接口，可以在线程池中执行。
 * 每个任务负责导入一个批次的数据，并处理其内部的事务。
 * 现在它依赖于一个BatchImportStrategy来执行实际的导入逻辑。
 *
 * @param <T> 导入数据的POJO类型
 */
@Slf4j
public class ExcelImportBatchTask<T> implements Callable<List<Map<String, String>>> {

    private final List<T> batchData; // 当前批次的数据 (已转换为POJO)
    private final String importJobId; // 导入任务ID
    private final BatchImportStrategy<T> importStrategy; // 导入策略
    private final TransactionTemplate transactionTemplate; // 事务模板

    /**
     * 构造函数。
     * @param batchData 当前批次的数据 (POJO列表)
     * @param importJobId 导入任务ID
     * @param importStrategy 导入策略实例
     * @param transactionTemplate 事务模板
     */
    public ExcelImportBatchTask(List<T> batchData, String importJobId,
                                BatchImportStrategy<T> importStrategy,
                                TransactionTemplate transactionTemplate) {
        this.batchData = batchData;
        this.importJobId = importJobId;
        this.importStrategy = importStrategy;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public List<Map<String, String>> call() throws Exception {
        List<Map<String, String>> failedRecords = new ArrayList<>(); // 收集当前批次导入失败的记录

        // 使用TransactionTemplate显式管理事务，确保批次导入的原子性
        transactionTemplate.execute(status -> {
            try {
                log.info(String.format("任务ID: %s - 开始处理批次数据 (大小: %d)", importJobId, batchData.size()));
                // 委托给具体的导入策略执行导入逻辑
                List<Map<String, String>> currentBatchFailed = importStrategy.importBatch(batchData);
                if (currentBatchFailed != null) {
                    failedRecords.addAll(currentBatchFailed);
                }
            } catch (Exception e) {
                // 捕获任何异常，标记事务回滚
                status.setRollbackOnly();
                log.error(String.format("任务ID: %s - 批次处理异常，标记回滚: %s", importJobId, e.getMessage()));
                // 将整个批次的数据都标记为失败，因为事务已回滚
                for (T item : batchData) {
                    // 尝试设置错误信息，如果POJO支持
                    failedRecords.add(importStrategy.t2Map(item));
                }
            }
            return null;
        });

        log.info(String.format("任务ID: %s - 批次处理完成，失败记录数: %d", importJobId, failedRecords.size()));
        return failedRecords; // 返回当前批次的失败记录
    }
}