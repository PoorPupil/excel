package com.ccl.excel.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 导出注解，用于标记需要进行异步 Excel 导出的方法。
 * 该注解会触发 AOP 切面，在后台线程中执行数据查询和 Excel 文件生成。
 */
@Retention(RetentionPolicy.RUNTIME) // 运行时保留注解，以便通过反射读取
@Target(ElementType.METHOD)     // 只能应用于方法上
public @interface ExcelExport {

    /**
     * @return 每次批量处理的数据行数。默认1000行。
     */
    int batchSize() default 1000;

    /**
     * @return 主线程等待所有子线程完成的最长秒数。如果超时，主线程将返回，但子线程会继续执行。默认30秒。
     */
    long timeoutSeconds() default 30;

    /**
     * @return 导出任务的名称，用于日志记录和前端显示。
     */
    String exportName() default "";

    /**
     * @return 指定用于处理导出逻辑的 BatchExportStrategy 实现类的 Spring Bean 名称。
     */
    String strategyBeanName();
}
