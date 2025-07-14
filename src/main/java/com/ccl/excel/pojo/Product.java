package com.ccl.excel.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 产品数据模型，用于Excel导入。
 * 包含一个用于存储导入失败原因的字段。
 */
@TableName("t_product")
public class Product {

    @TableId
    private String productId;
    private String productName;
    private BigDecimal price;

    private Integer stock;
    private String importError; // 导入失败原因

    // Constructors, Getters, Setters
    public Product() {
    }

    public Product(String productId, String productName, BigDecimal price, Integer stock) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImportError() {
        return importError;
    }

    public void setImportError(String importError) {
        this.importError = importError;
    }

    @Override
    public String toString() {
        return "Product{" +
               "productId='" + productId + '\'' +
               ", productName='" + productName + '\'' +
               ", price=" + price +
               ", stock=" + stock +
               ", importError='" + importError + '\'' +
               '}';
    }

    /**
     * 将Product对象转换为Map，用于错误报告生成。
     * @return 包含Product数据的Map
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("产品ID", productId);
        map.put("产品名称", productName);
        map.put("价格", price != null ? price.toPlainString() : "");
        map.put("库存", stock != null ? stock.toString() : "");
        map.put("失败原因", importError != null ? importError : "");
        return map;
    }
}