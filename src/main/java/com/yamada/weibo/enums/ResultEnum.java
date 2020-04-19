package com.yamada.weibo.enums;

import lombok.Getter;

@Getter
public enum ResultEnum {
    NAME_INTERVAL(-14, "修改昵称30天内无法再次修改"),
    USER_BAN(-13, "该用户已被封禁，无法进行相关操作"),
    CANNOT_APPEAL_REPEAT(-12, "不能重复举报"),
    MESSAGE_NOT_EXIST(-11, "该消息不存在"),
    SCHOOL_NOT_EXIST(-10, "未填写学校"),
    NAME_REPETITION(-9, "该昵称已存在"),
    USER_NOT_EXIST(-8, "该用户不存在"),
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
