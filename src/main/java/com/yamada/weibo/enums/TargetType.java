package com.yamada.weibo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TargetType {

    WEIBO(0, "微博"),
    COMMENT(1, "评论"),
    USER(2, "用户")
    ;

    private Integer code;

    private String target;
}
