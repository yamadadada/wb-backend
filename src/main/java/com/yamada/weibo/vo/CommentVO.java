package com.yamada.weibo.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yamada.weibo.utils.DateTimeSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CommentVO {

    private Integer cid;

    private Integer uid;

    private String name;

    private String avatar;

    private Integer wid;

    private String content;

    private Integer commentType;

    @JsonSerialize(using = DateTimeSerializer.class)
    private Date createTime;

    // 该评论回复的评论的cid
    private Integer commentCid;

    // 该评论回复的评论的用户名称
    private String commentName;

    private Integer likeCount;

    private Boolean isLike;

    // 评论该评论的list
    private List<CommentVO> commentVOList;

    public void addComment(CommentVO commentVO) {
        if (this.commentVOList == null) {
            this.commentVOList = new ArrayList<>();
        }
        this.commentVOList.add(commentVO);
    }
}
