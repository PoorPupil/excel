package com.ccl.excel.annotion;

import com.ccl.excel.strategy.BatchImportStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标记需要自动导入Excel的方法。
 * 通过此注解，可以配置批处理大小和主线程等待子线程的最长超时时间。
 */
@Retention(RetentionPolicy.RUNTIME) // 运行时保留注解，以便通过反射读取
@Target(ElementType.METHOD)     // 只能应用于方法上
public @interface ExcelImport {

    /**
     * @return 每次批量处理的数据行数。默认1000行。
     */
    int batchSize() default 1000;

    /**
     * @return 主线程等待所有子线程完成的最长秒数。如果超时，主线程将返回，但子线程会继续执行。默认30秒。
     */
    long timeoutSeconds() default 30;

    /**
     * @return 指定用于处理导入逻辑的BatchImportStrategy实现类。
     */
    Class<? extends BatchImportStrategy<?>> strategy();


}