package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class School {

    @TableId
    private Integer id;

    private String name;

    private String sid;

    private String department;

    private String city;

    private String level;
}
