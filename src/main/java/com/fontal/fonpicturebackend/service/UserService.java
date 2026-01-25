package com.fontal.fonpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fontal.fonpicturebackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fontal.fonpicturebackend.model.dto.user.UserQueryPage;
import com.fontal.fonpicturebackend.model.dto.user.UserUpdateRequest;
import com.fontal.fonpicturebackend.model.vo.user.UserLoginVo;
import com.fontal.fonpicturebackend.model.vo.user.UserVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author fontal
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2026-01-25 10:36:35
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   账号
     * @param email         邮箱
     * @param userPassWord  密码
     * @param checkPassword 确认密码
     * @return 注册用户ID
     */
    Long userRegister(String userAccount, String email, String userPassWord, String checkPassword);

    /**
     * 用户登入
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @param request      用于获取session
     * @return 脱敏后的user信息
     */
    UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登入用户信息
     *
     * @param request 用于获取session
     * @return user信息
     */
    User currentUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request 用于获取session
     * @return true
     */
    boolean userLogout(HttpServletRequest request);


    String getEncryptPassword(String password);

    UserLoginVo userToLoginVO(User user);

    UserVo userToUserVO(User user);

    boolean userUpdate(UserUpdateRequest userUpdateRequest, HttpServletRequest request);

    Wrapper<User> getQueryWrapper(UserQueryPage userQueryPage);
}
