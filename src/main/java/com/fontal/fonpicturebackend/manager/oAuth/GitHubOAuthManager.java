package com.fontal.fonpicturebackend.manager.oAuth;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.fontal.fonpicturebackend.model.vo.oAuth.GithubUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubOAuthManager {

    @Value("${oauth.github.client-id}")
    private String clientId;

    @Value("${oauth.github.client-secret}")
    private String clientSecret;

    @Value("${oauth.github.redirect-uri}")
    private String redirectUri;

    /**
     * 用 code 换取 access_token
     */
    public String getAccessToken(String code) {
        String url = "https://github.com/login/oauth/access_token";

        try (HttpResponse response = HttpRequest.post(url)
                .form("client_id", clientId)
                .form("client_secret", clientSecret)
                .form("code", code)
                .form("redirect_uri", redirectUri)
                .execute()) {

            // 解析响应：access_token=xxx&scope=...
            return parseAccessToken(response.body());
        }
    }

    /**
     * 解析 access_token 响应
     * 响应格式：access_token=gho_xxx&scope=&token_type=bearer
     */
    private String parseAccessToken(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        // 解析 URL 编码的响应体
        String[] pairs = response.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && "access_token".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    /**
     * 获取 GitHub 用户信息
     */
    public GithubUserInfo getUserInfo(String accessToken) {
        String url = "https://api.github.com/user";

        try (HttpResponse response = HttpRequest.get(url)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .execute()) {

            String body = response.body();
            return JSONUtil.toBean(body, GithubUserInfo.class);
        }
    }

}