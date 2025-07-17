package com.ccl.excel.strategy;

import java.util.List;

/**
 * 批量导出策略接口。
 * 定义了获取总数据量和分段获取数据的方法，具体实现由业务方提供。
 *
 * @param <T> 导出数据对应的实体类型
 */
public interface BatchExportStrategy<T> {

    /**
     * 根据方法参数获取导出数据的总数量。
     *
     * @param methodArgs 原始方法的参数
     * @return 导出数据的总数量
     */
    long getTotalCount(Object... methodArgs);

    /**
     * 根据偏移量、限制数量和方法参数获取指定分段的数据。
     *
     * @param offset     数据偏移量
     * @param limit      每页限制数量
     * @param methodArgs 原始方法的参数
     * @return 指定分段的数据列表
     */
    List<T> fetchDataSegment(long offset, long limit, Object... methodArgs);

    /**
     * 获取 Excel 导出的表头。
     *
     * @return 表头列表
     */
    List<String> getHeaders();

    /**
     * 将实体对象转换为用于 Excel 导出的数据行。
     *
     * @param data 实体对象
     * @return 包含实体数据的字符串列表，每个字符串对应一个单元格
     */
    List<String> convertToRow(T data);
}