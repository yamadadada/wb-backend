package com.yamada.weibo.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ForwardForm {

    @NotBlank(message = "转发内容不能为空")
    @Size(max = 1024, message = "字数不能超过1024个字")
    private String content;

    @NotNull(message = "微博ID不能为空")
    private Integer wid;
}
