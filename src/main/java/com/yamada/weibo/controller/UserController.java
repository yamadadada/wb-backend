package com.yamada.weibo.controller;

import com.yamada.weibo.service.UserService;
import com.yamada.weibo.utils.ResultUtil;
import com.yamada.weibo.utils.ServletUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public Object getUser() {
        Integer uid = ServletUtil.getUid();
        return ResultUtil.success(userService.getUserInfo(uid));
    }

    @GetMapping("/{uid}")
    public Object getUserById(@PathVariable("uid") Integer uid) {
        return ResultUtil.success(userService.getUserInfo(uid));
    }
}
