package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class WeiboTopic {

    @TableId
    private Integer wid;

    @TableId
    private Integer tid;
}
