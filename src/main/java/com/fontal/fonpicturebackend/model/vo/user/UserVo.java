package com.fontal.fonpicturebackend.model.vo.user;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 243423423451L;

    /**
     * 用户ID（需要将LONG转为String,否则在json.parse()时，丢失精度）
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 注册时间
     */
    private Date createTime;
}
