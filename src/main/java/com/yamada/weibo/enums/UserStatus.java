package com.yamada.weibo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {

    NORMAL(0, "正常"),
    BAN(1, "封禁")
    ;

    private Integer code;

    private String status;
}
