package com.yamada.weibo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BadgeVO {

    private Integer atCount;

    private Integer commentCount;

    private Integer likeCount;

    private Integer systemCount;

    private Integer totalCount;
}
