package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Appeal {

    @TableId(type = IdType.AUTO)
    private Integer aid;

    private Integer targetId;

    private Integer targetType;

    private Integer appealType;

    private String content;

    private Integer uid;

    private Date createTime;

    private Integer status;
}
