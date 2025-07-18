package com.ccl.excel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring 应用配置类，用于配置线程池。
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 配置用于 Excel 导入任务的线程池。
     * @return ThreadPoolTaskExecutor实例
     */
    @Bean(name = "excelImportTaskExecutor")
    public Executor excelImportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(2, Runtime.getRuntime().availableProcessors())); // 核心线程数，至少2个或CPU核心数
        executor.setMaxPoolSize(Math.max(5, Runtime.getRuntime().availableProcessors() * 2)); // 最大线程数，至少5个或CPU核心数*2
        executor.setQueueCapacity(200); // 任务队列容量
        executor.setThreadNamePrefix("ExcelImport-"); // 线程名称前缀
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略
        // 优雅停机：等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60); // 等待60秒
        executor.initialize();
        return executor;
    }

    /**
     * 配置用于 Excel 导出任务的线程池。
     * 这个线程池仅用于多线程查询数据处理需要导出的数据，
     * 主要是从数据库查询数据，属于IO密集部分
     * 其次是处理数据，属于轻量的CPU密集型
     * 线程数 ≈ CPU 核心数 × (1 + IO等待时间 / 计算时间)
     *  但是我们又要考虑内存，可能 下面的配置比较合适
     * @return ThreadPoolTaskExecutor实例
     */
    @Bean(name = "excelExportTaskExecutor")
    public Executor excelExportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(2, Runtime.getRuntime().availableProcessors())); // 核心线程数，至少2个或CPU核心数
        executor.setMaxPoolSize(Math.max(5, Runtime.getRuntime().availableProcessors() * 2)); // 最大线程数，至少5个或CPU核心数*2
        executor.setQueueCapacity(200); // 任务队列容量，适当增大以应对突发任务
        executor.setThreadNamePrefix("ExcelExport-"); // 线程名称前缀
        // 使用CallerRunsPolicy拒绝策略，当任务被拒绝时，由提交任务的线程（即调用者线程）直接执行该任务
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        // 优雅停机：等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60); // 等待60秒
        executor.initialize();
        return executor;
    }
}