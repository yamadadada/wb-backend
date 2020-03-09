package com.yamada.weibo.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserVO {

    private Integer uid;

    private String name;

    private String avatar;

    private String introduction;

    private Integer gender;

    private Date birth;

    private String location;

    private String school;

    // 微博数
    private Integer weiboCount;

    // 关注数
    private Integer followCount;

    // 粉丝数
    private Integer fanCount;
}
