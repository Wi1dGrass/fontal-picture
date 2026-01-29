package com.fontal.fonpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户修改密码请求
 */
@Data
public class UpdatePasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 243423423451L;

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String checkPassword;
}
