package com.ccl.excel.mapper;

import com.ccl.excel.pojo.ImportRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导入记录的模拟仓库，用于模拟数据库操作。
 * 在实际应用中，这将是一个JPA Repository或MyBatis Mapper。
 */
@Slf4j
@Repository
public class ImportRecordRepository {
    // 使用ConcurrentHashMap模拟数据库存储
    private final ConcurrentHashMap<String, ImportRecord> records = new ConcurrentHashMap<>();

    /**
     * 保存或更新导入记录。
     * @param record 导入记录对象
     * @return 保存后的导入记录
     */
    public ImportRecord save(ImportRecord record) {
        records.put(record.getId(), record);
        log.info("模拟DB操作: 保存/更新导入记录: " + record);
        return record;
    }

    /**
     * 根据ID查找导入记录。
     * @param id 导入记录ID
     * @return 导入记录Optional
     */
    public Optional<ImportRecord> findById(String id) {
        return Optional.ofNullable(records.get(id));
    }

    /**
     * 更新导入记录的状态和结束时间。
     * @param record 导入记录对象
     */
    public void update(ImportRecord record) {
        records.put(record.getId(), record);
        log.info("模拟DB操作: 更新导入记录: " + record);
    }
}