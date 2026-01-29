package com.fontal.fonpicturebackend.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 用户表
 * {@code @TableName}  user
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户密码(MD5)
     */
    private String userPassword;

    /**
     * 用户头像URL
     */
    private String userAvatar;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 认证方式：email/github/google
     */
    private String provider;

    /**
     * 第三方平台用户ID
     */
    private String providerId;

    /**
     * 第三方头像URL(备份)
     */
    private String thirdPartyAvatar;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDelete;


    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 243423423451L;

}