package com.yamada.weibo.enums;

import lombok.Getter;

@Getter
public enum ResultEnum {
    NOT_AUTH(-7, "你没有权限进行这个操作"),
    COMMENT_NOT_EXIST(-6, "该评论不存在"),
    WEIBO_NOT_EXIST(-5, "该微博不存在"),
    OPERATE_ERROR(-4, "操作失败，请稍后再试"),
    TOKEN_ERROR(-3, "登录已过期，请重新登录"),
    PARAMS_ERROR(-2, "请求参数错误"),
    BUSY(-1, "系统繁忙，请稍候再试"),
    SUCCESS(0, "请求成功"),
    CODE_INVALID(40029, "code无效"),
    FREQUENCY_LIMIT(45011, "请求次数超过限制")
    ;

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
