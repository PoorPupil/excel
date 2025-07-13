CREATE TABLE `t_import_record`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `task_name`     VARCHAR(100) NOT NULL comment '任务名称',
    `original_file` VARCHAR(255) NOT NULL comment '原始文件路径',
    `error_file`    VARCHAR(255) DEFAULT NULL comment '错误文件路径',
    `status`        tinyint      NOT NULL comment '任务状态, 0:处理中, 1:完成, 2:超时, 3:失败',
    `start_time`    DATETIME     NOT NULL,
    `end_time`      DATETIME     DEFAULT NULL
);