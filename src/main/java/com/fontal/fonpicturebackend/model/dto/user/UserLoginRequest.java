package com.fontal.fonpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录请求
 */
@Data
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 243423423451L;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户密码
     */
    private String userPassword;
}
