package com.yamada.weibo.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yamada.weibo.utils.DateSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class UserVO {

    private Integer uid;

    private String name;

    private String avatar;

    private String introduction;

    private Integer gender;

    @JsonSerialize(using = DateSerializer.class)
    private Date birth;

    private String location;

    private String school;

    // 注册时间
    private String registerTime;

    // 微博数
    private Integer weiboCount;

    // 关注数
    private Integer followCount;

    // 粉丝数
    private Integer fanCount;

    private Boolean isFollow;
}
