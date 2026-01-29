-- 创建数据库
CREATE DATABASE IF NOT EXISTS `fontal-picture` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `fontal-picture`;

-- 删除旧表（如果需要保留数据，先备份）
DROP TABLE IF EXISTS `user`;

-- 创建新用户表
CREATE TABLE `user`
(
    id                  BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `userName`          VARCHAR(50) DEFAULT 'User' COMMENT '用户昵称',
    `email`             VARCHAR(100) NOT NULL COMMENT '用户邮箱',
    `userPassword`      VARCHAR(256) COMMENT '用户密码(MD5)',
    `userAvatar`        VARCHAR(1024) COMMENT '用户头像URL',
    `userRole`          VARCHAR(20) DEFAULT 'user' NOT NULL COMMENT '用户角色',
    `userProfile`       VARCHAR(500) COMMENT '用户简介',

    -- OAuth 相关
    `provider`          VARCHAR(20) DEFAULT 'email' COMMENT '认证方式：email/github/google',
    `providerId`        VARCHAR(100) COMMENT '第三方平台用户ID',
    `thirdPartyAvatar`  VARCHAR(1024) COMMENT '第三方头像URL(备份)',

    -- 状态相关
    `status`            TINYINT DEFAULT 1 NOT NULL COMMENT '状态：0-禁用，1-正常',
    `lastLoginTime`     DATETIME COMMENT '最后登录时间',
    `lastLoginIp`       VARCHAR(50) COMMENT '最后登录IP',

    -- 时间字段
    `createTime`        DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updateTime`        DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleteTime`        DATETIME COMMENT '删除时间',
    `isDelete`          TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_provider` (`provider`, `providerId`),
    INDEX `idx_status` (`status`),
    INDEX `idx_provider` (`provider`),
    INDEX `idx_createTime` (`createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 删除deleteTime
ALTER TABLE `user` DROP COLUMN `deleteTime`;

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

-- 用户传图：通过管理员审核
ALTER TABLE picture
    -- 添加新列
    ADD COLUMN reviewStatus  INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    ADD COLUMN reviewMessage VARCHAR(512)  NULL COMMENT '审核信息',
    ADD COLUMN reviewerId    BIGINT        NULL COMMENT '审核人 ID',
    ADD COLUMN reviewTime    DATETIME      NULL COMMENT '审核时间';

-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);




