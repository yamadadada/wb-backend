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

    @PutMapping("/{cid}")
    public Object like(@PathVariable("cid") Integer cid) {
        commentLikeService.like(cid);
        return ResultUtil.success(null);
    }

    @DeleteMapping("/{cid}")
    public Object notLike(@PathVariable("cid") Integer cid) {
        commentLikeService.notLike(cid);
        return ResultUtil.success(null);
    }
}
