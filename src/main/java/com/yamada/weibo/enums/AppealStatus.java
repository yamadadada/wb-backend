package com.yamada.weibo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppealStatus {

    APPEAL(0, "已举报"),
    DONE(1, "已处理")
    ;

    private Integer code;

    private String status;
}
