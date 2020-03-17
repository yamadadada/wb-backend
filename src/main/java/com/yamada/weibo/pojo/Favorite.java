package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Favorite {

    @TableId
    private Integer uid;

    @TableId
    private Integer wid;

    private Date createTime;
}
