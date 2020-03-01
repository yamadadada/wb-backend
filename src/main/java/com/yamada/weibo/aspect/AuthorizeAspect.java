package com.yamada.weibo.aspect;

import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.utils.JwtUtil;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Aspect
@Component
public class AuthorizeAspect {

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthorizeAspect(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Pointcut("execution(public * com.yamada.weibo.controller.*.*(..)) " +
            "&& !execution(public * com.yamada.weibo.controller.AuthController.login(..))")
    public void verify() {

    }

    @Before("verify()")
    public void doVerify() {
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new MyException(ResultEnum.TOKEN_ERROR);
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("token");
        Integer uid = jwtUtil.verify(token);
        HttpSession session = request.getSession();
        // 将uid存进session
        session.setAttribute("uid", uid);
    }
}
