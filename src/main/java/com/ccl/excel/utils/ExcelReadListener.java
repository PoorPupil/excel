package com.ccl.excel.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * SAX事件模型下的Excel读取监听器，用于高效处理大型.xlsx文件。
 * 实现XSSFSheetXMLHandler.SheetContentsHandler接口，逐行解析数据并按批次处理。
 */
@Slf4j
public class ExcelReadListener implements XSSFSheetXMLHandler.SheetContentsHandler {

    private final int batchSize; // 批处理大小
    private final Consumer<List<Map<String, String>>> batchConsumer; // 批处理数据消费者
    private List<Map<String, String>> currentBatch; // 当前批次的数据
    private Map<String, String> currentRow; // 当前行的数据
    private int currentRowNum; // 当前行号
    private List<String> header; // 表头信息

    /**
     * 构造函数。
     * @param batchSize 批处理大小
     * @param batchConsumer 批处理数据消费者，当收集到一批数据时调用
     */
    public ExcelReadListener(int batchSize, Consumer<List<Map<String, String>>> batchConsumer) {
        this.batchSize = batchSize;
        this.batchConsumer = batchConsumer;
        this.currentBatch = new ArrayList<>(batchSize); // 预分配容量
        this.currentRowNum = -1; // 从-1开始，因为第一行是表头
    }

    /**
     * 处理Excel文件。
     * @param xlsxFileStream Excel文件的输入流
     * @param sharedStringsTable 共享字符串表
     * @throws Exception 如果处理过程中发生错误
     */
    public void process(InputStream xlsxFileStream, SharedStrings sharedStringsTable) throws Exception {
        OPCPackage pkg = OPCPackage.open(xlsxFileStream);
        XSSFReader xssfReader = new XSSFReader(pkg);
        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

        // 获取第一个工作表的输入流
        InputStream sheet2 = xssfReader.getSheetsData().next();
        InputSource sheetSource = new InputSource(sheet2);

        // 创建SheetContentsHandler，并将其与XMLReader关联
        XSSFSheetXMLHandler handler = new XSSFSheetXMLHandler(null, sharedStringsTable, this, false);
        parser.setContentHandler(handler);

        // 解析工作表
        parser.parse(sheetSource);

        sheet2.close();
        pkg.close();

        // 处理剩余的不足一个批次的数据
        if (!currentBatch.isEmpty()) {
            batchConsumer.accept(new ArrayList<>(currentBatch)); // 传递副本
            currentBatch.clear();
        }
    }

    @Override
    public void startRow(int rowNum) {
        this.currentRowNum = rowNum;
        this.currentRow = new LinkedHashMap<>(); // 使用LinkedHashMap保持列顺序
    }

    @Override
    public void endRow(int rowNum) {
        if (rowNum == 0) { // 第一行是表头
            this.header = new ArrayList<>(currentRow.values());
            log.info("Excel表头: " + header);
        } else {
            // 将当前行数据添加到批次中
            currentBatch.add(new LinkedHashMap<>(currentRow)); // 传递副本
            if (currentBatch.size() >= batchSize) {
                batchConsumer.accept(new ArrayList<>(currentBatch)); // 传递副本
                currentBatch.clear();
            }
        }
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
        // 获取列索引，例如 A1 -> 0, B1 -> 1
        int colIndex = getColumnIndex(cellReference);
        if (colIndex >= 0) {
            if (currentRowNum == 0) { // 表头行，存储列名
                currentRow.put(String.valueOf(colIndex), formattedValue);
            } else if (header != null && colIndex < header.size()) { // 数据行，使用表头作为键
                currentRow.put(header.get(colIndex), formattedValue);
            } else { // 如果没有表头或者列索引超出表头范围，使用列索引作为键
                currentRow.put(String.valueOf(colIndex), formattedValue);
            }
        }
    }

//    @Override
//    public void headerFooter(String text, boolean is  Header, String tagName) {
//        // 不处理页眉页脚
//    }

    /**
     * 从单元格引用（如"A1", "B2"）中获取列索引。
     * @param cellReference 单元格引用字符串
     * @return 列索引 (0-based)
     */
    private int getColumnIndex(String cellReference) {
        String colRef = cellReference.replaceAll("\\d", ""); // 移除数字部分
        int colIndex = -1;
        for (char c : colRef.toCharArray()) {
            colIndex = (colIndex + 1) * 26 + (c - 'A');
        }
        return colIndex;
    }
}