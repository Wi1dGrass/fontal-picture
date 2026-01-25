package com.fontal.fonpicturebackend.aop;

import com.fontal.fonpicturebackend.annotation.AuthCheck;
import com.fontal.fonpicturebackend.exception.BusinessException;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.model.domain.User;
import com.fontal.fonpicturebackend.model.enums.UserRoleEnum;
import com.fontal.fonpicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterCeptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前用户
        User loginUser = userService.currentUser(request);
        String userRole = loginUser.getUserRole();
        //如果mustRole为空，放行
        if (mustRole == null) {
            return joinPoint.proceed();
        }
        //如果userRole为空，抛出未登录异常
        if (userRole == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //如果mustRole为admin且userRole不为admin，抛出无权限
        if (mustRole.equals(UserRoleEnum.ADMIN.getValue()) && !userRole.equals(UserRoleEnum.ADMIN.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过校验
        return joinPoint.proceed();
    }
}
