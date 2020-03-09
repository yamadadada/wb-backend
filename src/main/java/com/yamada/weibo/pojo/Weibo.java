package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class Weibo {

    @TableId(type = IdType.AUTO)
    private Integer wid;

    private Integer uid;

    @NotBlank(message = "微博内容不能为空")
    @Size(max = 140, message = "字数不能超过140个字")
    private String content;

    private Integer status;

    private Integer baseForwardWid;

    private String forwardLink;

    private String images;

    private Date createTime;
}
