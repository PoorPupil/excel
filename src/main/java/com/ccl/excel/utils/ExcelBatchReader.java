package com.ccl.excel.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // For .xlsx files
import org.apache.poi.hssf.usermodel.HSSFWorkbook; // For .xls files

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelBatchReader {

    private static final int BATCH_SIZE = 1000; // 每批次处理1000行数据

    public static void readExcelInBatches(String filePath) {
        File excelFile = new File(filePath);
        if (!excelFile.exists()) {
            log.error("文件不存在: " + filePath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = getWorkbook(filePath, fis)) {

            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
            int lastRowNum = sheet.getLastRowNum(); // 获取最后一行的索引

            // todo 应该在这里开始开子线程

            List<List<String>> currentBatch = new ArrayList<>();
            int processedRows = 0;

            // 从第二行开始（假设第一行是标题）
            for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue; // 跳过空行
                }

                List<String> rowData = new ArrayList<>();
                // 遍历单元格，获取数据
                for (Cell cell : row) {
                    rowData.add(getCellValueAsString(cell));
                }
                currentBatch.add(rowData);

                processedRows++;

                // 当达到批次大小时，处理当前批次数据
                if (currentBatch.size() >= BATCH_SIZE || rowNum == lastRowNum) {
                    // todo 这里的 processBatch 应该调用用户自己新增的实现类的方法。
                    processBatch(currentBatch, processedRows);
                    currentBatch.clear(); // 清空当前批次，释放内存
                }
            }
            log.info("Excel 数据读取完成，总共处理行数: " + processedRows);

        } catch (IOException e) {
            log.error("读取 Excel 文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Workbook getWorkbook(String filePath, FileInputStream fis) throws IOException {
        if (filePath.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (filePath.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new IllegalArgumentException("不支持的文件格式，请使用 .xls 或 .xlsx 文件。");
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // 可以根据需要处理公式
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * 模拟处理批次数据的方法
     * 在实际应用中，这里会执行数据库插入、业务逻辑处理等操作
     */
    private static void processBatch(List<List<String>> batchData, int currentTotalProcessed) {
        log.info("--- 正在处理批次数据 ---");
        log.info("批次大小: " + batchData.size() + ", 已处理总行数: " + currentTotalProcessed);
        // 实际处理逻辑，例如：
        // for (List<String> row : batchData) {
        //     // 将 row 插入数据库
        //     // 执行其他业务逻辑
        // }
        // 模拟耗时操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("--- 批次数据处理完成 ---");
    }

    public static void main(String[] args) {
        // 替换为你的 Excel 文件路径
        String excelFilePath = "your_large_excel_file.xlsx";
        readExcelInBatches(excelFilePath);
    }
}