package com.yamada.weibo.vo;

import lombok.Data;

@Data
public class TextVO {

    private String text;

    private Integer textType;

    private Integer uid;

    private String name;

    private String topic;
}
