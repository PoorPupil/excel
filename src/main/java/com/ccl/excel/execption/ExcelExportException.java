package com.ccl.excel.execption;

/**
 * 自定义 Excel 导出异常。
 */
public class ExcelExportException extends RuntimeException {
    public ExcelExportException(String message) {
        super(message);
    }

    public ExcelExportException(String message, Throwable cause) {
        super(message, cause);
    }
}