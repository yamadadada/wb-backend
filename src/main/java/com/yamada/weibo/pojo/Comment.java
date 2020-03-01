package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Comment {

    @TableId(type = IdType.AUTO)
    private Integer cid;

    private Integer uid;

    private Integer wid;

    private String content;

    private Integer commentCid;

    private String commentName;

    private Integer commentType;

    private Date createTime;
}
