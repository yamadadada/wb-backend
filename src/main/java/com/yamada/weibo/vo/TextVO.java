package com.yamada.weibo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TextVO implements Serializable {

    private static final long serialVersionUID = -7502881045846994551L;

    private String text;

    private Integer textType;

    private Integer uid;

    private String name;

    private String topic;
}
