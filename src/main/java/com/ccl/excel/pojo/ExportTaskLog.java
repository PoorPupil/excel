package com.ccl.excel.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ccl.excel.constant.ExportStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 导出任务日志实体类。
 * 记录每次异步 Excel 导出的详细信息。
 */
@Data
@TableName("t_export_task_log") // 更改表名为 t_export_task_log
public class ExportTaskLog {

    @TableId
    private String id; // 导出任务的唯一标识符
    private String exportName; // 导出任务的名称
    private LocalDateTime startTime; // 导出开始时间
    private LocalDateTime endTime; // 导出结束时间
    private Integer status; // 导出任务的状态 (使用 ExportStatus 枚举值)
    private Long totalRecords; // 总记录数
    private Long exportedRecords; // 已导出记录数
    private String filePath; // 导出 Excel 文件的路径
    private String errorMessage; // 错误信息
    private String requestParams; // 原始请求参数的 JSON 字符串

    public ExportTaskLog() {
        this.id = UUID.randomUUID().toString(); // 自动生成ID
        this.startTime = LocalDateTime.now();
        this.status = ExportStatus.PENDING.getValue(); // 初始状态为待处理
        this.totalRecords = 0L;
        this.exportedRecords = 0L;
    }
}