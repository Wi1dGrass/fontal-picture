package com.fontal.fonpicturebackend.model.vo.user;

import java.util.Date;

public class UserVo {
    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 业务更新时间
     */
    private Date editTime;

}
