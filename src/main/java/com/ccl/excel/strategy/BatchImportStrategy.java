package com.ccl.excel.strategy;

import java.util.List;
import java.util.Map;

/**
 * 批处理导入策略接口。
 * 定义了将Excel行数据转换为POJO以及执行批处理导入的通用方法。
 *
 * @param <T> 导入数据的POJO类型
 */
public interface BatchImportStrategy<T> {

    /**
     * 将从Excel读取的原始行数据（Map<String, String>）转换为具体的业务POJO对象。
     * 这个方法应该包含数据类型转换、验证等逻辑。
     *
     * @param rowData 从Excel读取的一行数据，键为列名，值为字符串形式的单元格内容。
     * @return 转换后的业务POJO对象。如果转换失败或数据不合法，可以返回null或抛出异常。
     * 为了收集失败数据，建议返回一个包含错误信息的POJO，或者在内部记录错误。
     */
    T convertRow(Map<String, String> rowData);

    /**
     * 执行实际的批处理导入逻辑。
     * 这个方法应该包含业务验证、持久化（如保存到数据库）等操作。
     *
     * @param data 待导入的POJO对象列表。
     * @return 导入失败的POJO对象列表。如果所有数据都成功导入，则返回空列表。
     * 失败的POJO对象中应包含失败原因，以便生成错误报告。
     */
    List<Map<String, String>> importBatch(List<T> data);

    /**
     * 获取用于错误报告的列头。
     * @return 包含所有列名的列表，顺序应与错误报告一致。
     */
    List<String> getErrorHeaders();

    Map<String, String> t2Map(T item);
}