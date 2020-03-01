package com.yamada.weibo.enums;

import lombok.Getter;

@Getter
public enum CommentType {

    LEVEL1(0, "第一层评论"),
    LEVEL2(1, "第二层评论")
    ;

    private Integer code;

    private String content;

    CommentType(Integer code, String content) {
        this.code = code;
        this.content = content;
    }
}
