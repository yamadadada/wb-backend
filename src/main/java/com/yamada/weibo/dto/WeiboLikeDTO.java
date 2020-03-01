package com.yamada.weibo.dto;

import lombok.Data;

@Data
public class WeiboLikeDTO {

    private Integer wid;

    private Integer likeCount;

    private Boolean isLike;
}
