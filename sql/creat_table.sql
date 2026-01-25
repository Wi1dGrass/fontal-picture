-- 创建数据库
CREATE DATABASE IF NOT EXISTS `fontal-picture` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `fontal-picture`;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `userName`     VARCHAR(256) COMMENT '用户名',
    `userAccount`  VARCHAR(256) NOT NULL UNIQUE COMMENT '用户账号',
    `userPassword` VARCHAR(256) NOT NULL COMMENT '用户密码',
    `userAvatar`   VARCHAR(1024) COMMENT '用户头像URL',
    `email`        VARCHAR(512) COMMENT '用户邮箱',
    `userRole`     VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
    `userProfile`  VARCHAR(512) COMMENT '用户简介',
    `editTime`     DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '业务更新时间',
    `createTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userAccount` (`userAccount`),
    INDEX `idx_userName` (`userName`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),
    INDEX idx_introduction (introduction(100)), -- 前缀索引
    INDEX idx_category (category),
    INDEX idx_tags (tags(100)),                  -- 前缀索引
    INDEX idx_userId (userId)
) comment '图片' collate = utf8mb4_unicode_ci;


