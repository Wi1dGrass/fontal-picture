package com.fontal.fonpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求
 */
@Data
public class UserRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 243423423451L;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;
}
