package com.yamada.weibo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TextType {

    NORMAL(0, "普通文本"),
    AT(1, "@文本"),
    TOPIC(2, "话题文本")
    ;

    private Integer code;

    private String type;
}
