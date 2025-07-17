package com.ccl.excel.constant;

/**
 * 导出任务状态枚举。
 */
public enum ExportStatus {
    PENDING(0, "待处理"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "失败"),
    TIMED_OUT(4, "主线程超时，后台继续");

    private final int value;
    private final String description;

    ExportStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}