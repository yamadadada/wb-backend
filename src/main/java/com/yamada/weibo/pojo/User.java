package com.yamada.weibo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.Date;

@Data
public class User {

    @TableId(type = IdType.AUTO)
    private Integer uid;

    private String openid;

    private String sessionKey;

    @NotBlank(message = "昵称不能为空")
    @Pattern(regexp = "^[\\w\\u4e00-\\u9fa5]{1,16}$", message = "昵称格式不正确")
    private String name;

    private String avatar;

    @Size(max = 32, message = "简介长度太长")
    private String introduction;

    private Date createTime;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别格式不正确")
    @Max(value = 2, message = "性别格式不正确")
    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birth;

    @Size(max = 32, message = "所在地长度太长")
    private String location;

    @Size(max = 16, message = "学校长度太长")
    private String school;

    private Integer status;
}
