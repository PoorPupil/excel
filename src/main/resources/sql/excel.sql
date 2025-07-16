-- 创建表
create table `t_import_record`
(
    `id`            varchar(64) PRIMARY KEY,
    `file_name`     varchar(255) NOT NULL,
    `start_time`    datetime     NOT NULL,
    `end_time`      datetime     DEFAULT NULL,
    `status`        tinyint      NOT NULL,
    `failed_report_path` varchar(255) DEFAULT NULL
);


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
    `id`    varchar(64) Primary key,
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



INSERT INTO `t_user` (`id`, `name`, `age`, `email`)
VALUES ('1a2b3c01', '张三', 25, 'zhangsan@example.com'),
       ('1a2b3c02', '李四', 30, 'lisi@example.com'),
       ('1a2b3c03', '王五', 28, 'wangwu@example.com'),
       ('1a2b3c04', '赵六', 35, 'zhaoliu@example.com'),
       ('1a2b3c05', '孙七', 22, 'sunqi@example.com'),
       ('1a2b3c06', '周八', 31, 'zhouba@example.com'),
       ('1a2b3c07', '吴九', 29, 'wujiu@example.com'),
       ('1a2b3c08', '郑十', 26, 'zhengshi@example.com'),
       ('1a2b3c09', '冯十一', 33, 'feng11@example.com'),
       ('1a2b3c10', '陈十二', 27, 'chen12@example.com'),
       ('1a2b3c11', '褚十三', 36, 'chu13@example.com'),
       ('1a2b3c12', '卫十四', 24, 'wei14@example.com'),
       ('1a2b3c13', '蒋十五', 38, 'jiang15@example.com'),
       ('1a2b3c14', '沈十六', 21, 'shen16@example.com'),
       ('1a2b3c15', '韩十七', 40, 'han17@example.com'),
       ('1a2b3c16', '杨十八', 23, 'yang18@example.com'),
       ('1a2b3c17', '朱十九', 34, 'zhu19@example.com'),
       ('1a2b3c18', '秦二十', 32, 'qin20@example.com'),
       ('1a2b3c19', '尤二一', 26, 'you21@example.com'),
       ('1a2b3c20', '许二二', 37, 'xu22@example.com');
