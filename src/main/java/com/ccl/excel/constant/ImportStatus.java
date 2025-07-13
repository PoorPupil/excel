package com.ccl.excel.constant;

/**
 * 定义Excel导入任务的状态。
 */
public enum ImportStatus {
    STARTED,        // 导入任务已开始
    IN_PROGRESS,    // 导入任务正在进行中（主线程已返回，子线程仍在后台执行）
    COMPLETED_SUCCESS, // 导入任务已完成，所有数据成功导入
    COMPLETED_WITH_ERRORS, // 导入任务已完成，部分数据导入失败
    FAILED          // 导入任务因异常而失败
}