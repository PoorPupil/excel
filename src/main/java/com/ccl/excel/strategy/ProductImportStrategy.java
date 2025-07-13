package com.ccl.excel.strategy;

import com.ccl.excel.pojo.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 产品数据导入策略实现。
 * 模拟产品数据的验证和导入逻辑。
 */
@Slf4j
@Component
public class ProductImportStrategy implements BatchImportStrategy<Product> {

    @Override
    public Product convertRow(Map<String, String> rowData) {
        Product product = new Product();
        String productId = rowData.get("产品ID");
        String productName = rowData.get("产品名称");
        String priceStr = rowData.get("价格");
        String stockStr = rowData.get("库存");

        StringBuilder errorBuilder = new StringBuilder();

        if (productId == null || productId.trim().isEmpty()) {
            errorBuilder.append("产品ID不能为空; ");
        }
        if (productName == null || productName.trim().isEmpty()) {
            errorBuilder.append("产品名称不能为空; ");
        }
        BigDecimal price = null;
        if (priceStr != null && !priceStr.trim().isEmpty()) {
            try {
                price = new BigDecimal(priceStr);
            } catch (NumberFormatException e) {
                errorBuilder.append("价格格式不正确; ");
            }
        } else {
            errorBuilder.append("价格不能为空; ");
        }
        Integer stock = null;
        if (stockStr != null && !stockStr.trim().isEmpty()) {
            try {
                stock = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                errorBuilder.append("库存格式不正确; ");
            }
        } else {
            errorBuilder.append("库存不能为空; ");
        }

        product.setProductId(productId);
        product.setProductName(productName);
        product.setPrice(price);
        product.setStock(stock);

        if (errorBuilder.length() > 0) {
            product.setImportError(errorBuilder.toString().trim());
        }
        return product;
    }



    @Override
    public List<Map<String, String>> importBatch(List<Product> products) {
        List<Map<String, String>> failedProducts = new ArrayList<>();
        log.info("--- ProductImportStrategy: 开始导入批次产品数据 (大小: " + products.size() + ") ---");
        for (Product product : products) {
            if (product.getImportError() != null && !product.getImportError().isEmpty()) {
                failedProducts.add(t2Map(product));
                log.error("ProductImportStrategy: 导入失败 (转换错误): " + product);
                continue;
            }

            // 模拟业务逻辑验证和持久化
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                product.setImportError("价格必须为正数");
                failedProducts.add(t2Map(product));
                log.error("ProductImportStrategy: 导入失败 (业务验证): " + product);
            } else if (product.getStock() == null || product.getStock() < 0) {
                product.setImportError("库存不能为负数");
                failedProducts.add(t2Map(product));
                log.error("ProductImportStrategy: 导入失败 (业务验证): " + product);
            } else {
                // 模拟成功保存到数据库
                log.info("ProductImportStrategy: 成功导入产品: " + product.getProductName());
            }
        }
        log.info("--- ProductImportStrategy: 批次产品数据导入完成，失败数: " + failedProducts.size() + " ---");
        return failedProducts;
    }

    @Override
    public List<String> getErrorHeaders() {
        return Arrays.asList("产品ID", "产品名称", "价格", "库存", "失败原因");
    }

    @Override
    public Map<String, String> t2Map(Product item) {
        Map<String, String> failedRecord = new LinkedHashMap<>();
        failedRecord.put("产品ID", item.getProductId());
        failedRecord.put("产品名称", item.getProductName());
        failedRecord.put("价格", item.getPrice() != null ? item.getPrice().toString() : "");
        failedRecord.put("库存", item.getStock() != null ? item.getStock().toString() : "");
        failedRecord.put("失败原因", item.getImportError());
        return failedRecord;
    }
}