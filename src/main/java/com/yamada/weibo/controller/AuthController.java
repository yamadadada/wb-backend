package com.yamada.weibo.controller;

import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.form.UserInfoForm;
import com.yamada.weibo.service.AuthService;
import com.yamada.weibo.utils.ResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public Object login(@RequestParam("code") String code) {
        if (StringUtils.isBlank(code)) {
            throw new MyException(ResultEnum.PARAMS_ERROR);
        }
        Map<String, Object> map = authService.login(code);
        return ResultUtil.success(map);
    }

    @PostMapping("/getUserInfo")
    public Object getUserInfo(@RequestBody @Valid UserInfoForm userInfoForm, BindingResult result) {
        if (result.hasErrors()) {
            throw new MyException(result.getAllErrors().get(0).getDefaultMessage());
        }
        Map<String, Object> map = authService.getUserInfo(userInfoForm);
        return ResultUtil.success(map);
    }
}
