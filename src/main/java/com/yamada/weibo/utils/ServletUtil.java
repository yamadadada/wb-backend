package com.yamada.weibo.utils;

import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ServletUtil {

    public static String getFromSession(String key) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new MyException(ResultEnum.TOKEN_ERROR);
        }
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession();
        return (String)session.getAttribute(key);
    }

    public static Integer getUid() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new MyException(ResultEnum.TOKEN_ERROR);
        }
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession();
        return (Integer)session.getAttribute("uid");
    }
}
