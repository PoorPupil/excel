package com.ccl.excel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ccl.excel.pojo.ExportTaskLog;

/**
 * 导出任务日志服务接口。
 */
public interface ExportTaskLogService extends IService<ExportTaskLog> {

    /**
     * 创建并保存一个新的导出任务日志。
     * @param exportTaskLog 导出任务日志实体
     */
    void createExportLog(ExportTaskLog exportTaskLog);

    /**
     * 更新导出任务日志的状态。
     * @param id 任务ID
     * @param status 新状态
     * @param errorMessage 错误信息 (如果失败)
     */
    void updateExportLogStatus(String id, Integer status, String errorMessage);

    /**
     * 最终更新导出任务日志，包括结束时间、状态、文件路径和导出记录数。
     * @param id 任务ID
     * @param status 最终状态
     * @param filePath 文件路径
     * @param exportedRecords 导出记录数
     * @param errorMessage 错误信息
     */
    void finalizeExportLog(String id, Integer status, String filePath, Long exportedRecords, String errorMessage);
}