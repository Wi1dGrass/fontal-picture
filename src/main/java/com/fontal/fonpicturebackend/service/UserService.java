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
     * @param userName      昵称
     * @param email         邮箱
     * @param userPassWord  密码
     * @param checkPassword 确认密码
     * @return 注册用户ID
     */
    UserLoginVo userRegister(String userName, String email, String userPassWord, String checkPassword);

    /**
     * 用户登入
     *
     * @param email 账号
     * @param userPassword 密码
     * @param request      用于获取session
     * @return 脱敏后的user信息
     */
    UserLoginVo userLogin(String email, String userPassword, HttpServletRequest request);

    /**
     * GitHub OAuth 登入
     *
     * @param code    GitHub 授权码
     * @param request 用于获取 session 和 IP
     * @return 脱敏后的用户信息
     */
    UserLoginVo githubOAuthLogin(String code, HttpServletRequest request);

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

    /**
     * 加密密码
     *
     * @param password 密码
     * @return 加密密码
     */
    String getEncryptPassword(String password);

    /**
     * 将user转为vo
     * @param user user
     * @return userLoginVo
     */
    UserLoginVo userToLoginVO(User user);

    /**
     * 将user转为vo
     * @param user user
     * @return userVo
     */
    UserVo userToUserVO(User user);

    /**
     * 更新user信息
     * @param userUpdateRequest user更新请求
     * @param request session
     * @return 是否成功 true
     */
    boolean userUpdate(UserUpdateRequest userUpdateRequest, HttpServletRequest request);

    /**
     * 查询
     * @param userQueryPage user查询参数
     * @return 查询对象
     */
    Wrapper<User> getQueryWrapper(UserQueryPage userQueryPage);

    /**
     * 是否为管理员
     *
     * @param user 用户对象
     * @return true/false
     */
    boolean isAdmin(User user);

    /**
     * 修改密码
     *
     * @param oldPassword  原密码
     * @param newPassword  新密码
     * @param checkPassword 确认新密码
     * @param request     用于获取session
     * @return 是否成功 true
     */
    boolean updatePassword(String oldPassword, String newPassword, String checkPassword, HttpServletRequest request);
}
