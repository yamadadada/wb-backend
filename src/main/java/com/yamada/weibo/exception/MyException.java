package com.yamada.weibo.exception;

import com.yamada.weibo.enums.ResultEnum;
import lombok.Getter;

@Getter
public class MyException extends RuntimeException {

    private Integer code;

    public MyException(String message) {
        super(message);
        this.code = -1;
    }

    public MyException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }

    public MyException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
