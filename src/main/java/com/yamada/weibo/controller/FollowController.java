package com.yamada.weibo.controller;

import com.yamada.weibo.service.FollowService;
import com.yamada.weibo.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    @Autowired
    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{uid}")
    public Object addFollow(@PathVariable("uid") Integer uid) {
        followService.addFollow(uid);
        return ResultUtil.success(null);
    }

    @DeleteMapping("/{uid}")
    public Object deleteFollow(@PathVariable("uid") Integer uid) {
        followService.deleteFollow(uid);
        return ResultUtil.success(null);
    }
}
