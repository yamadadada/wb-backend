package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Follow {

    @TableId
    private Integer uid;

    @TableId
    private Integer followUid;

    private Date followTime;
}
