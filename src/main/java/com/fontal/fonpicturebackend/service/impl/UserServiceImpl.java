package com.fontal.fonpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fontal.fonpicturebackend.constant.UserConstant;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.exception.ThrowUtils;
import com.fontal.fonpicturebackend.model.domain.User;
import com.fontal.fonpicturebackend.model.dto.user.UserQueryPage;
import com.fontal.fonpicturebackend.model.dto.user.UserUpdateRequest;
import com.fontal.fonpicturebackend.model.enums.UserRoleEnum;
import com.fontal.fonpicturebackend.model.vo.user.UserLoginVo;
import com.fontal.fonpicturebackend.model.vo.user.UserVo;
import com.fontal.fonpicturebackend.service.UserService;
import com.fontal.fonpicturebackend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.support.CustomSQLErrorCodesTranslation;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Objects;

import static com.fontal.fonpicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author fontal
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2026-01-25 10:36:35
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Override
    public Long userRegister(String userAccount, String email, String userPassWord, String checkPassword) {
        // 1. 校验参数非空
        ThrowUtils.throwIf(userAccount == null || userAccount.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "用户账号不能为空");
        ThrowUtils.throwIf(email == null || email.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(userPassWord == null || userPassWord.isEmpty(), ErrorCode.PARAMS_ERROR, "用户密码不能为空");
        ThrowUtils.throwIf(checkPassword == null || checkPassword.isEmpty(), ErrorCode.PARAMS_ERROR, "校验密码不能为空");

        // 2. 校验账号长度
        ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 20, ErrorCode.PARAMS_ERROR, "用户账号长度必须在4-20个字符之间");

        // 3. 校验账号格式
        ThrowUtils.throwIf(!userAccount.matches("^[a-zA-Z0-9_]+$"), ErrorCode.PARAMS_ERROR, "用户账号只能包含字母、数字和下划线");

        // 4. 校验密码长度
        ThrowUtils.throwIf(userPassWord.length() < 6 || userPassWord.length() > 20, ErrorCode.PARAMS_ERROR, "用户密码长度必须在6-20个字符之间");

        // 5. 校验两次密码是否一致
        ThrowUtils.throwIf(!checkPassword.equals(userPassWord), ErrorCode.PARAMS_ERROR, "两次密码不一致");

        // 6. 校验邮箱格式
        ThrowUtils.throwIf(!email.matches("^[A-Za-z0-9+_.-]+@(.+)$"), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");

        // 7. 检查用户账号是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");

        // 8. 将密码加盐后md5加密
        String EncryptPassword = getEncryptPassword(userPassWord);

        // 9. 数据封装
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(EncryptPassword);
        user.setEmail(email);
        user.setUserName("User");
        user.setUserRole(UserRoleEnum.USER.getValue());

        // 10. 保存在数据库中
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "数据库操作失败");
        return user.getId();
    }

    @Override
    public UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验参数非空
        ThrowUtils.throwIf(userAccount == null || userAccount.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "用户账号不能为空");
        ThrowUtils.throwIf(userPassword == null || userPassword.isEmpty(), ErrorCode.PARAMS_ERROR, "用户密码不能为空");

        // 2. 加密密码
        String encryptPassword = getEncryptPassword(userPassword);

        // 3. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);

        // 4. 校验用户是否存在
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "账号或密码错误");

        // 5. 更新session状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.userToLoginVO(user);
    }

    @Override
    public User currentUser(HttpServletRequest request) {
        Object objUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) objUser;
        //校验用户是否登入
        ThrowUtils.throwIf(user == null || user.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        Long userId = user.getId();
        //从数据库中获取user信息，防止从session中获取信息与数据库信息不一致
        user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);

        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //判断是否登入
        Object objUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(objUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //移除session
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public String getEncryptPassword(String password) {
        String SALT = "fontal";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    @Override
    public UserLoginVo userToLoginVO(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtils.copyProperties(user, userLoginVo);
        return userLoginVo;
    }

    @Override
    public UserVo userToUserVO(User user) {
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    @Override
    public boolean userUpdate(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        // 1. 校验请求参数不为空
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        // 2. 校验 id 不为空
        Long id = userUpdateRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        // 3. 获取其他参数
        String userName = userUpdateRequest.getUserName();
        String userAvatar = userUpdateRequest.getUserAvatar();
        String userProfile = userUpdateRequest.getUserProfile();
        String email = userUpdateRequest.getEmail();
        String userRole = userUpdateRequest.getUserRole();

        // 4. 校验用户名（如果提供）
        if (userName != null && !userName.trim().isEmpty()) {
            ThrowUtils.throwIf(userName.length() > 20, ErrorCode.PARAMS_ERROR, "用户名长度不能超过20个字符");
        }

        // 5. 校验用户头像URL（如果提供）
        if (userAvatar != null && !userAvatar.trim().isEmpty()) {
            ThrowUtils.throwIf(userAvatar.length() > 1024, ErrorCode.PARAMS_ERROR, "用户头像URL长度不能超过1024个字符");
        }

        // 6. 校验用户简介（如果提供）
        if (userProfile != null && !userProfile.trim().isEmpty()) {
            ThrowUtils.throwIf(userProfile.length() > 512, ErrorCode.PARAMS_ERROR, "用户简介长度不能超过512个字符");
        }

        // 7. 校验邮箱格式（如果提供）
        if (email != null && !email.trim().isEmpty()) {
            ThrowUtils.throwIf(!email.matches("^[A-Za-z0-9+_.-]+@(.+)$"), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 8. 校验用户角色（如果提供）
        if (userRole != null && !userRole.trim().isEmpty()) {
            ThrowUtils.throwIf(
                !UserRoleEnum.USER.getValue().equals(userRole) && !UserRoleEnum.ADMIN.getValue().equals(userRole),
                ErrorCode.PARAMS_ERROR,
                "用户角色只能是 user 或 admin"
            );
        }

        // 9. 校验权限
        User objUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(objUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 判断是否有权限：只能修改自己的信息，或者是管理员
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(objUser.getUserRole());
        ThrowUtils.throwIf(!Objects.equals(id, objUser.getId()) && !isAdmin, ErrorCode.NO_AUTH_ERROR, "无权限修改");

        // 10. 检查用户是否存在
        User existUser = this.getById(id);
        ThrowUtils.throwIf(existUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 11. 更新数据
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "数据库操作异常");

        return true;
    }

    @Override
    public Wrapper<User> getQueryWrapper(UserQueryPage userQueryPage) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 1. 如果存在 searchText，则使用 OR 查询多个字段
        String searchText = userQueryPage.getSearchText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("userName", searchText)
                .or()
                .like("userProfile", searchText)
                .or()
                .like("email", searchText)
            );
        } else {
            // 2. 如果没有 searchText，则根据具体字段精确查询

            // 根据 ID 查询
            Long id = userQueryPage.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }

            // 根据用户名模糊查询
            String userName = userQueryPage.getUserName();
            if (userName != null && !userName.trim().isEmpty()) {
                queryWrapper.like("userName", userName);
            }

            // 根据用户简介模糊查询
            String userProfile = userQueryPage.getUserProfile();
            if (userProfile != null && !userProfile.trim().isEmpty()) {
                queryWrapper.like("userProfile", userProfile);
            }

            // 根据邮箱模糊查询
            String email = userQueryPage.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                queryWrapper.like("email", email);
            }

            // 根据用户角色精确查询
            String userRole = userQueryPage.getUserRole();
            if (userRole != null && !userRole.trim().isEmpty()) {
                queryWrapper.eq("userRole", userRole);
            }
        }

        // 3. 排除已删除的用户
        queryWrapper.eq("isDelete", 0);

        // 4. 默认按创建时间降序排序
        queryWrapper.orderByDesc("createTime");

        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole().equals(UserConstant.ADMIN_ROLE);
    }
}




