package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Topic {

    @TableId
    private Integer tid;

    private String name;

    private Integer uid;

    private Date createTime;
}
