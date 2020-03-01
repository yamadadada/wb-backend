package com.yamada.weibo.controller;

import com.yamada.weibo.service.CommentLikeService;
import com.yamada.weibo.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/commentLike")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @Autowired
    public CommentLikeController(CommentLikeService commentLikeService) {
        this.commentLikeService = commentLikeService;
    }

    @PutMapping("/{wid}")
    public Object like(@PathVariable("wid") Integer wid) {
        commentLikeService.like(wid);
        return ResultUtil.success(null);
    }

    @DeleteMapping("/{wid}")
    public Object notLike(@PathVariable("wid") Integer wid) {
        commentLikeService.notLike(wid);
        return ResultUtil.success(null);
    }
}
