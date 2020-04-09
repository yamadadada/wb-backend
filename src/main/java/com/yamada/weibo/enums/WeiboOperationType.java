package com.yamada.weibo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeiboOperationType {

    FORWARD(0, "转发"),
    COMMENT(1, "评论"),
    LIKE(2, "赞")
    ;

    private Integer code;

    private String type;
}
