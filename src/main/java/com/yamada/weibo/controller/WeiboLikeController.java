package com.yamada.weibo.controller;

import com.yamada.weibo.service.WeiboLikeService;
import com.yamada.weibo.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weiboLike")
public class WeiboLikeController {

    private final WeiboLikeService weiboLikeService;

    @Autowired
    public WeiboLikeController(WeiboLikeService weiboLikeService) {
        this.weiboLikeService = weiboLikeService;
    }

    @PutMapping("/{wid}")
    public Object like(@PathVariable("wid") Integer wid) {
        weiboLikeService.like(wid);
        return ResultUtil.success(null);
    }

    @DeleteMapping("/{wid}")
    public Object notLike(@PathVariable("wid") Integer wid) {
        weiboLikeService.notLike(wid);
        return ResultUtil.success(null);
    }
}
