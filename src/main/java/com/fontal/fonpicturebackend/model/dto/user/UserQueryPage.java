package com.fontal.fonpicturebackend.model.dto.user;

import com.fontal.fonpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryPage extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 243423423451L;

    /**
     * 搜索文本（用于模糊查询用户名、简介、邮箱）
     */
    private String searchText;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

}
