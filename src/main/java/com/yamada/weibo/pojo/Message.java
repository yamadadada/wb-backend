package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yamada.weibo.utils.FullTimeSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class Message {

    @TableId(type = IdType.AUTO)
    private Integer mid;

    private Integer uid;

    private Integer sendUid;

    private String sendName;

    private String sendAvatar;

    private String content;

    private String image;

    private String baseContent;

    private Integer wid;

    private Integer cid;

    private Integer type;

    @JsonSerialize(using = FullTimeSerializer.class)
    private Date createTime;
}
