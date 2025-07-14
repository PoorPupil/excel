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

create TABLE `t_user`
(
    `id`    varchar(64) Primary key ,
    `name`  varchar(32) default '' comment '名称',
    `age`   int         default 0 comment '年龄',
    `email` varchar(32) default '' comment '邮箱'
);

create TABLE `t_product`
(
    `product_id`   varchar(64) Primary key,
    `product_name` varchar(64)    not null default '' comment '产品名称',
    `price`        DECIMAL(12, 2) not null default 0.00 comment '价格',
    `stock`        int            not null default 0 comment '库存'
);