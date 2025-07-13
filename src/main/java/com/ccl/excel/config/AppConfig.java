package com.ccl.excel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Executor;

/**
 * Spring应用配置类，用于配置线程池和事务管理器。
 */
@Slf4j
@Configuration
public class AppConfig {

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

    /**
     * 配置编程式事务模板。
     * @param transactionManager 平台事务管理器
     * @return TransactionTemplate实例
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    // 模拟PlatformTransactionManager，实际项目中会由Spring Boot自动配置或您手动配置DataSourceTransactionManager等
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new MockPlatformTransactionManager();
    }

    // 模拟PlatformTransactionManager，用于演示，实际项目中不需要
    private static class MockPlatformTransactionManager implements PlatformTransactionManager {
        @Override
        public org.springframework.transaction.TransactionStatus getTransaction(org.springframework.transaction.TransactionDefinition definition) throws org.springframework.transaction.TransactionException {
            log.info("模拟事务: 获取事务 - " + definition.getName());
            return new org.springframework.transaction.support.SimpleTransactionStatus(true); // 总是返回一个新事务
        }

        @Override
        public void commit(org.springframework.transaction.TransactionStatus status) throws org.springframework.transaction.TransactionException {
            log.info("模拟事务: 提交事务");
        }

        @Override
        public void rollback(org.springframework.transaction.TransactionStatus status) throws org.springframework.transaction.TransactionException {
            log.info("模拟事务: 回滚事务");
        }
    }
}