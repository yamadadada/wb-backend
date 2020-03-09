package com.yamada.weibo.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.service.WeiboService;
import com.yamada.weibo.utils.ResultUtil;
import com.yamada.weibo.vo.WeiboLikeVO;
import com.yamada.weibo.vo.WeiboVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/weibo")
public class WeiboController {

    private final WeiboService weiboService;

    @Autowired
    public WeiboController(WeiboService weiboService) {
        this.weiboService = weiboService;
    }

    /**
     * 获得关注的微博
     * @param page
     * @param size
     * @return
     */
    @GetMapping("")
    public Object getFollowWeibo(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResultUtil.success(weiboService.getFollowWeibo(page, size));
    }

    /**
     * 加载微博正文页面
     * @param wid
     * @return
     */
    @GetMapping("/{wid}")
    public Object getWeiboDetail(@PathVariable("wid") Integer wid) {
        return ResultUtil.success(weiboService.getWeiboDetail(wid));
    }

    /**
     * 获得该微博的转发列表
     * @param wid
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/forward/{wid}")
    public Object getWeiboForward(@PathVariable("wid") Integer wid,
                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                  @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        List<WeiboVO> weiboVOList = weiboService.getWeiboForward(wid);
        Map<String, Object> map = new HashMap<>();
        map.put("forwardList", weiboVOList);
        map.put("forwardCount", new PageInfo<>(weiboVOList).getTotal());
        return ResultUtil.success(map);
    }

    /**
     * 获得该微博的评论列表
     * @param wid
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/comment/{wid}")
    public Object getWeiboComment(@PathVariable("wid") Integer wid, @RequestParam("sort") String sort,
                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                  @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        return ResultUtil.success(weiboService.getWeiboComment(wid, sort));
    }

    /**
     * 获得该微博的点赞列表
     * @param wid
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/like/{wid}")
    public Object getWeiboLike(@PathVariable("wid") Integer wid,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        List<WeiboLikeVO> weiboLikeVOList = weiboService.getWeiboLike(wid);
        Map<String, Object> map = new HashMap<>();
        map.put("weiboLikeVOList", weiboLikeVOList);
        map.put("likeCount", new PageInfo<>(weiboLikeVOList).getTotal());
        return ResultUtil.success(map);
    }

    @PostMapping("")
    public Object add(@RequestBody Weibo weibo, BindingResult result) {
        if (result.hasErrors()) {
            throw new MyException(result.getAllErrors().get(0).toString());
        }
        Integer wid = weiboService.add(weibo);
        return ResultUtil.success(wid);
    }

    @DeleteMapping("/{wid}")
    public Object delete(@PathVariable("wid") Integer wid) {
        weiboService.delete(wid);
        return ResultUtil.success(null);
    }

    @PostMapping("/upload/{wid}")
    public Object upload(@PathVariable("wid") Integer wid, @RequestParam("file") MultipartFile file) {
        weiboService.upload(wid, file);
        return ResultUtil.success(null);
    }
}
