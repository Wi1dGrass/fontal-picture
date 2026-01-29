package com.fontal.fonpicturebackend.model.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户登录返回视图
 */
@Data
public class UserLoginVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 243423423451L;

    /**
     * 用户ID（需要将LONG转为String,否则在json.parse()时，丢失精度）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名
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


}
