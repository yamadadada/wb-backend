package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class Comment {

    @TableId(type = IdType.AUTO)
    private Integer cid;

    private Integer uid;

    @NotNull(message = "微博ID不能为空")
    private Integer wid;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1024, message = "评论字数不能超过1024个字")
    private String content;

    private Integer commentCid;

    private String commentName;

    private Integer commentUid;

    private Integer commentType;

    private Date createTime;
}
