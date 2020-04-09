package com.yamada.weibo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {

    AT(0, "@"),
    COMMENT(1, "评论"),
    LIKE(2, "点赞"),
    SYSTEM(3, "系统消息")
    ;

    private Integer code;

    private String type;
}
