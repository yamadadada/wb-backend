package com.yamada.weibo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultVO {

    private Integer code;

    private String msg;

    private Object data;

    public ResultVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
