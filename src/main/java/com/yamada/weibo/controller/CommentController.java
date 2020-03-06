package com.yamada.weibo.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.pojo.Comment;
import com.yamada.weibo.service.CommentService;
import com.yamada.weibo.utils.ResultUtil;
import com.yamada.weibo.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 进入评论详情页
     * @param cid
     * @return
     */
    @GetMapping("/{cid}")
    public Object getCommentDetail(@PathVariable("cid") Integer cid) {
        Map<String, Object> map = new HashMap<>();
        CommentVO commentVO = commentService.getByCid(cid);
        PageHelper.startPage(1, 10);
        List<CommentVO> commentVOList = commentService.getLevel2ByCid(cid, "like_count");
        commentVO.setCommentVOList(commentVOList);
        map.put("commentVO", commentVO);

        PageInfo<CommentVO> pageInfo = new PageInfo<>(commentVOList);
        map.put("count", pageInfo.getTotal());
        return ResultUtil.success(map);
    }

    /**
     * 获得第二层评论
     * @param cid
     * @param sort
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/{cid}/comment")
    public Object getLevel2CommentByCid(@PathVariable("cid") Integer cid, @RequestParam("sort") String sort,
                                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Map<String, Object> map = new HashMap<>();
        PageHelper.startPage(page, size);
        List<CommentVO> commentVOList = commentService.getLevel2ByCid(cid, sort);
        map.put("commentVOList", commentVOList);

        PageInfo<CommentVO> pageInfo = new PageInfo<>(commentVOList);
        map.put("count", pageInfo.getTotal());
        return ResultUtil.success(map);
    }

    @PostMapping("")
    public Object add(@RequestBody @Validated Comment comment, BindingResult result) {
        if (result.hasErrors()) {
            throw new MyException(result.getAllErrors().get(0).toString());
        }
        if (StringUtils.isBlank(comment.getCommentName())) {
            comment.setCommentName(null);
        }
        commentService.add(comment);
        return ResultUtil.success(null);
    }

    @DeleteMapping("/{cid}")
    public Object delete(@PathVariable("cid") Integer cid) {
        commentService.delete(cid);
        return ResultUtil.success(null);
    }
}
