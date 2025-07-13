package com.ccl.excel.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 错误报告Excel文件生成工具类。
 */
@Slf4j
public class ExcelReportGenerator {

    /**
     * 生成包含失败记录的Excel文件。
     * @param failedRecords 失败的记录列表，每个Map代表一行数据
     * @param filePath 生成文件的完整路径
     * @param headers 报告的列头，按顺序排列
     * @throws IOException 如果写入文件失败
     */
    public static void generateErrorExcel(List<Map<String, String>> failedRecords, String filePath, List<String> headers) throws IOException {
        if (failedRecords == null || failedRecords.isEmpty()) {
            log.info("没有失败记录，无需生成错误报告。");
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("导入失败报告");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        int colNum = 0;
        for (String header : headers) {
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(header);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowNum = 1;
        for (Map<String, String> rowData : failedRecords) {
            Row row = sheet.createRow(rowNum++);
            colNum = 0;
            for (String header : headers) {
                Cell cell = row.createCell(colNum++);
                cell.setCellValue(rowData.getOrDefault(header, ""));
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            log.info("错误报告Excel文件已生成: " + filePath);
        } finally {
            workbook.close();
        }
    }
}