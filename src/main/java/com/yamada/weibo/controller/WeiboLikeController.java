package com.yamada.weibo.controller;

import com.yamada.weibo.enums.WeiboOperationType;
import com.yamada.weibo.service.WeiboLikeService;
import com.yamada.weibo.service.WeiboService;
import com.yamada.weibo.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weiboLike")
public class WeiboLikeController {

    private final WeiboLikeService weiboLikeService;

    private final WeiboService weiboService;

    @Autowired
    public WeiboLikeController(WeiboLikeService weiboLikeService, WeiboService weiboService) {
        this.weiboLikeService = weiboLikeService;
        this.weiboService = weiboService;
    }

    @PutMapping("/{wid}")
    public Object like(@PathVariable("wid") Integer wid) {
        weiboLikeService.like(wid);
        weiboService.increaseScore(wid, WeiboOperationType.LIKE);
        return ResultUtil.success(null);
    }

    @DeleteMapping("/{wid}")
    public Object notLike(@PathVariable("wid") Integer wid) {
        weiboLikeService.notLike(wid);
        weiboService.decreaseScore(wid, WeiboOperationType.LIKE);
        return ResultUtil.success(null);
    }
}
