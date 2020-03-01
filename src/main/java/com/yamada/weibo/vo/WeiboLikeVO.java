package com.yamada.weibo.vo;

import lombok.Data;

import java.util.Date;

@Data
public class WeiboLikeVO {

    private Integer uid;

    private Integer wid;

    private Date createTime;

    private String name;

    private String avatar;

    private String introduction;
}
