package com.yamada.weibo.vo;

import lombok.Data;

@Data
public class LoginResponse {

    private String openid;

    private String sessionKey;

    private String unionid;

    private Integer errcode;

    private String errmsg;
}
