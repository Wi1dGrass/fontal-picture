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
     * 用户名
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户头像URL
     */
    private String userAvatar;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 业务更新时间
     */
    private Date editTime;

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