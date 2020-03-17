package com.yamada.weibo.controller;

import com.github.pagehelper.PageHelper;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.service.UserService;
import com.yamada.weibo.utils.ResultUtil;
import com.yamada.weibo.utils.ServletUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/{uid}")
    public Object update(@PathVariable("uid") Integer uid, @RequestBody @Validated User user, BindingResult result) {
        if (result.hasErrors()) {
            throw new MyException(result.getAllErrors().get(0).getDefaultMessage());
        }
        userService.update(uid, user);
        return ResultUtil.success(null);
    }

    @GetMapping("/myFollow")
    public Object myFollow(@RequestParam(value = "page", defaultValue = "1") Integer page,
                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        return ResultUtil.success(userService.getSimpleUserVO("uid"));
    }

    @GetMapping("/myFans")
    public Object myFans(@RequestParam(value = "page", defaultValue = "1") Integer page,
                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        return ResultUtil.success(userService.getSimpleUserVO("follow_uid"));
    }

    @PostMapping("/updateAvatar/{uid}")
    public Object updateAvatar(@PathVariable("uid") Integer uid, @RequestParam("file") MultipartFile file) {
        userService.updateAvatar(uid, file);
        return ResultUtil.success(null);
    }

    @GetMapping("/searchByName")
    public Object searchByName(@RequestParam("name") String name) {
        return ResultUtil.success(userService.searchByName(name));
    }

    @GetMapping("/userIndex")
    public Object getUserIndex() {
        return ResultUtil.success(userService.getUserIndex());
    }
}
