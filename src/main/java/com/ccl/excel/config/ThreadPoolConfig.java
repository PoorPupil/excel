package com.ccl.excel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring应用配置类，用于配置线程池和事务管理器。
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 配置用于Excel导入任务的线程池。
     * @return ThreadPoolTaskExecutor实例
     */
    @Bean(name = "excelImportTaskExecutor")
    public Executor excelImportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 核心线程数
        executor.setMaxPoolSize(10); // 最大线程数
        executor.setQueueCapacity(200); // 任务队列容量
        executor.setThreadNamePrefix("ExcelImport-"); // 线程名称前缀
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略
        executor.initialize();
        return executor;
    }

}