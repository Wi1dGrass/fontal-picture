package com.fontal.fonpicturebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Spring Session 配置 - Session 持久化到 Redis
 */
@Configuration
public class SessionConfig {

    /**
     * 配置 Cookie 序列化
     * 设置 Cookie 名称，支持跨域共享
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // 设置 Cookie 名称
        serializer.setCookieName("fontal_session");
        // 设置 Cookie 路径为根路径，支持跨域
        serializer.setCookiePath("/");
        // 设置 Cookie 域名（如果需要跨子域名共享，可以设置为 .yourdomain.com）
        // serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        // 使用 httpOnly 防止 XSS 攻击
        serializer.setUseHttpOnlyCookie(true);
        // 生产环境建议开启 secure（仅 HTTPS）
        // serializer.setUseSecureCookie(true);
        return serializer;
    }

    /**
     * 配置 Redis 序列化方式，使用 JSON 序列化
     */
    @Bean
    public RedisSerializer springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
