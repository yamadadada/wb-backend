package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Forward {

    @TableId
    private Integer wid;

    @TableId
    private Integer forwardWid;

    private Date createTime;
}
