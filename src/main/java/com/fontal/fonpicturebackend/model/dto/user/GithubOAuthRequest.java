package com.fontal.fonpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * GitHub OAuth 登录请求
 */
@Data
public class GithubOAuthRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * GitHub 授权码
     */
    private String code;
}