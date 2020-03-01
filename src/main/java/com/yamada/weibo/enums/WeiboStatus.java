package com.yamada.weibo.enums;

import lombok.Getter;

@Getter
public enum WeiboStatus {
    FORMAL(0, "普通"),
    FORWARD(1, "转发")
    ;

    private Integer code;

    private String content;

    WeiboStatus(Integer code, String content) {
        this.code = code;
        this.content = content;
    }
}
