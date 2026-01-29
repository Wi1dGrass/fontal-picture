package com.fontal.fonpicturebackend.model.vo.oAuth;

import lombok.Data;

@Data
  public class GithubUserInfo {
      private Long id;              // GitHub 用户 ID（用作 providerId）
      private String login;         // GitHub 用户名
      private String name;          // 昵称
      private String email;         // 邮箱（可能为 null）
      private String avatar_url;    // 头像 URL
      private String bio;           // 简介
      private String type;          // User/Org
  }