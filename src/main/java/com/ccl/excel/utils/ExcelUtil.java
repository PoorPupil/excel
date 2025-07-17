package com.ccl.excel.utils;

import com.ccl.excel.execption.ExcelExportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Excel 工具类，用于创建和写入 Excel 文件。
 */
public class ExcelUtil {

    /**
     * 创建 SXSSFWorkbook 实例。
     *
     * @return SXSSFWorkbook 实例
     */
    public static SXSSFWorkbook createWorkbook() {
        // 使用 SXSSFWorkbook 提高性能，避免 OOM
        SXSSFWorkbook workbook = new SXSSFWorkbook(100); // 内存中保留100行，超出则写入临时文件
        workbook.setCompressTempFiles(true); // 压缩临时文件
        return workbook;
    }

    /**
     * 创建并写入 Excel 表头。
     *
     * @param sheet   SXSSFSheet 实例
     * @param headers 表头列表
     */
    public static void writeHeaders(SXSSFSheet sheet, List<String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 写入数据行到 Excel。
     *
     * @param sheet SXSSFSheet 实例
     * @param rowNum 行号
     * @param rowData 行数据列表
     */
    public static void writeRow(SXSSFSheet sheet, int rowNum, List<String> rowData) {
        if (rowData == null || rowData.isEmpty()) {
            return;
        }
        Row dataRow = sheet.createRow(rowNum);
        for (int i = 0; i < rowData.size(); i++) {
            Cell cell = dataRow.createCell(i);
            cell.setCellValue(rowData.get(i));
        }
    }

    /**
     * 将 SXSSFWorkbook 写入字节数组。
     *
     * @param workbook SXSSFWorkbook 实例
     * @return 包含 Excel 文件内容的字节数组
     * @throws ExcelExportException 如果写入失败
     */
    public static byte[] writeWorkbookToByteArray(SXSSFWorkbook workbook) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new ExcelExportException("写入 Excel 文件到字节数组失败", e);
        } finally {
            try {
                workbook.close(); // 关闭 workbook 释放资源
                workbook.dispose(); // 释放临时文件
            } catch (IOException e) {
                // 忽略关闭异常
            }
        }
    }
}