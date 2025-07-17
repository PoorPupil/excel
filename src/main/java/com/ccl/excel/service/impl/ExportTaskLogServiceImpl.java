package com.ccl.excel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccl.excel.constant.ExportStatus;
import com.ccl.excel.mapper.ExportTaskLogMapper;
import com.ccl.excel.pojo.ExportTaskLog;
import com.ccl.excel.service.ExportTaskLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 导出任务日志服务实现类。
 */
@Slf4j
@Service
public class ExportTaskLogServiceImpl extends ServiceImpl<ExportTaskLogMapper, ExportTaskLog> implements ExportTaskLogService {

    @Override
    @Transactional
    public void createExportLog(ExportTaskLog exportTaskLog) {
        this.save(exportTaskLog);
        log.info("创建导出任务日志: {}", exportTaskLog.getId());
    }

    @Override
    @Transactional
    public void updateExportLogStatus(String id, Integer status, String errorMessage) {
        ExportTaskLog logEntry = new ExportTaskLog();
        logEntry.setId(id);
        logEntry.setStatus(status);
        if (errorMessage != null) {
            logEntry.setErrorMessage(errorMessage);
        }
        this.updateById(logEntry);
        log.info("更新导出任务日志状态: ID={}, 状态={}", id, ExportStatus.values()[status].getDescription());
    }

    @Override
    @Transactional
    public void finalizeExportLog(String id, Integer status, String filePath, Long exportedRecords, String errorMessage) {
        ExportTaskLog logEntry = new ExportTaskLog();
        logEntry.setId(id);
        logEntry.setEndTime(LocalDateTime.now());
        logEntry.setStatus(status);
        logEntry.setFilePath(filePath);
        logEntry.setExportedRecords(exportedRecords);
        logEntry.setErrorMessage(errorMessage);
        this.updateById(logEntry);
        log.info("最终更新导出任务日志: ID={}, 状态={}, 文件路径={}, 导出记录数={}",
                id, ExportStatus.values()[status].getDescription(), filePath, exportedRecords);
    }
}