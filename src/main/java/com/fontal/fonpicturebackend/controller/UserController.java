package com.fontal.fonpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fontal.fonpicturebackend.annotation.AuthCheck;
import com.fontal.fonpicturebackend.common.BaseResponse;
import com.fontal.fonpicturebackend.common.DeleteRequest;
import com.fontal.fonpicturebackend.common.ResultUtils;
import com.fontal.fonpicturebackend.constant.UserConstant;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.exception.ThrowUtils;
import com.fontal.fonpicturebackend.model.domain.User;
import com.fontal.fonpicturebackend.model.dto.user.*;
import com.fontal.fonpicturebackend.model.vo.user.UserLoginVo;
import com.fontal.fonpicturebackend.model.vo.user.UserVo;
import com.fontal.fonpicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<UserLoginVo> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        //校验参数
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        //取参数
        String userName = userRegisterRequest.getUserName();
        String email = userRegisterRequest.getEmail();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        //进入service层
        UserLoginVo userLoginVo = userService.userRegister(userName, email, userPassword, checkPassword);
        //返回结果
        return ResultUtils.success(userLoginVo);
    }

    /**
     * 用户登入
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        //取参数
        String email = userLoginRequest.getEmail();
        String userPassword = userLoginRequest.getUserPassword();
        //进入service层
        UserLoginVo userLoginVo = userService.userLogin(email, userPassword, request);
        //返回结果
        return ResultUtils.success(userLoginVo);
    }

    /**
     * GitHub OAuth 登入
     */
    @PostMapping("/oauth/github")
    public BaseResponse<UserLoginVo> githubOAuthLogin(@RequestBody GithubOAuthRequest githubOAuthRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(githubOAuthRequest == null, ErrorCode.PARAMS_ERROR);
        //取参数
        String code = githubOAuthRequest.getCode();
        //进入service层
        UserLoginVo userLoginVo = userService.githubOAuthLogin(code, request);
        //返回结果
        return ResultUtils.success(userLoginVo);
    }

    /**
     * 获取当前登入用户
     */
    @GetMapping("/current")
    public BaseResponse<UserVo> getCurrentUser(HttpServletRequest request) {
        User user = userService.currentUser(request);
        return ResultUtils.success(userService.userToUserVO(user));
    }

    /**
     * 用户注销
     */
    @GetMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 修改密码
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(updatePasswordRequest == null, ErrorCode.PARAMS_ERROR);
        // 取参数
        String oldPassword = updatePasswordRequest.getOldPassword();
        String newPassword = updatePasswordRequest.getNewPassword();
        String checkPassword = updatePasswordRequest.getCheckPassword();
        // 进入service层
        boolean result = userService.updatePassword(oldPassword, newPassword, checkPassword, request);
        // 返回结果
        return ResultUtils.success(result);
    }

    /**
     * 添加用户（管理员权限）
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        //校验userAddRequest
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        //获取参数到user中
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        //默认密码
        String defaultPassword = "123456789";
        String encryptPassword = userService.getEncryptPassword(defaultPassword);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "数据库操作异常");
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据id查询用户（管理员权限）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据id查询用户
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<UserVo> getUserVoById(long id) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        UserVo userVo = userService.userToUserVO(user);
        return ResultUtils.success(userVo);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> userUpdate(@RequestBody UserUpdateRequest userUpdateRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean save = userService.userUpdate(userUpdateRequest,request);
        return ResultUtils.success(save);
    }
    /**
     * 删除用户（管理员权限）
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> userDelete(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0,ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 分页查询（管理员权限）
     */
    @PostMapping("/query")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> userQueryPage(@RequestBody UserQueryPage userQueryPage){
        ThrowUtils.throwIf(userQueryPage == null,ErrorCode.PARAMS_ERROR);
        long current = userQueryPage.getCurrent();
        long pageSize = userQueryPage.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current,pageSize),
                userService.getQueryWrapper(userQueryPage));
        return ResultUtils.success(userPage);
    }
}