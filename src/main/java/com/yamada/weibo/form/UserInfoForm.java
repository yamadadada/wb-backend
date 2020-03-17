package com.yamada.weibo.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserInfoForm {

    @NotBlank(message = "用户昵称不能为空")
    private String name;

    @NotBlank(message = "头像不能为空")
    private String avatar;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别格式不正确")
    @Max(value = 2, message = "性别格式不正确")
    private Integer gender;

    private String country;

    private String province;

    private String city;
}
