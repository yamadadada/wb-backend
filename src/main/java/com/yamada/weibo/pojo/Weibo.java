package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Weibo {

    @TableId(type = IdType.AUTO)
    private Integer wid;

    private Integer uid;

    private String content;

    private Integer status;

    private Integer baseForwardWid;

    private String forwardLink;

    private String images;

    private Date createTime;
}
