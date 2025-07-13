package com.ccl.excel.pojo;

import com.ccl.excel.constant.ImportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 导入记录实体类，模拟数据库中的一条记录，用于追踪Excel导入任务的状态。
 */
public class ImportRecord {
    private String id; // 导入任务的唯一标识符
    private String fileName; // 导入的Excel文件名
    private LocalDateTime startTime; // 导入开始时间
    private LocalDateTime endTime; // 导入结束时间
    private ImportStatus status; // 导入任务的状态
    private String failedReportPath; // 失败报告Excel文件的路径

    public ImportRecord() {
        this.id = UUID.randomUUID().toString(); // 自动生成ID
        this.startTime = LocalDateTime.now();
        this.status = ImportStatus.STARTED;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ImportStatus getStatus() {
        return status;
    }

    public void setStatus(ImportStatus status) {
        this.status = status;
    }

    public String getFailedReportPath() {
        return failedReportPath;
    }

    public void setFailedReportPath(String failedReportPath) {
        this.failedReportPath = failedReportPath;
    }

    @Override
    public String toString() {
        return "ImportRecord{" +
               "id='" + id + '\'' +
               ", fileName='" + fileName + '\'' +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", status=" + status +
               ", failedReportPath='" + failedReportPath + '\'' +
               '}';
    }
}