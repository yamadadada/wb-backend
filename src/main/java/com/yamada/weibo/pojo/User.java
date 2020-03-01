package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class User {

    @TableId(type = IdType.AUTO)
    private Integer uid;

    private String openid;

    private String sessionKey;

    private String name;

    private String avatar;

    private String introduction;

    private Date createTime;

    private Integer gender;

    private Date birth;

    private String location;

    private String school;
}
