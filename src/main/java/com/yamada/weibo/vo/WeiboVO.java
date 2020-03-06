package com.yamada.weibo.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yamada.weibo.utils.Date2StringSerializer;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WeiboVO {

    private Integer wid;

    private Integer uid;

    private String content;

    private Integer status;

    private Integer baseForwardWid;

    private String forwardUsername;

    private String forwardAvatar;

    private String forwardContent;

    private List<String> forwardImageList;

    private List<String> imageList;

    @JsonSerialize(using = Date2StringSerializer.class)
    private Date createTime;

    private String fullTime;

    private String name;

    private String avatar;

    private Integer forwardCount;

    private Integer commentCount;

    private Integer likeCount;

    private Boolean isLike;

    private List<CommentVO> commentVOList;
}
