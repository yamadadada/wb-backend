package com.yamada.weibo.controller;

import com.github.pagehelper.PageHelper;
import com.yamada.weibo.enums.MessageType;
import com.yamada.weibo.service.MessageService;
import com.yamada.weibo.utils.ResultUtil;
import com.yamada.weibo.utils.ServletUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/at")
    public Object at(@RequestParam(value = "page", defaultValue = "1") Integer page,
                   @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        return ResultUtil.success(messageService.getList(MessageType.AT));
    }

    @GetMapping("/comment")
    public Object comment(@RequestParam(value = "page", defaultValue = "1") Integer page,
                     @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        return ResultUtil.success(messageService.getList(MessageType.COMMENT));
    }

    @GetMapping("/like")
    public Object like(@RequestParam(value = "page", defaultValue = "1") Integer page,
                          @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        return ResultUtil.success(messageService.getList(MessageType.LIKE));
    }

    @GetMapping("/system")
    public Object system(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        PageHelper.startPage(page, size);
        return ResultUtil.success(messageService.getList(MessageType.SYSTEM));
    }

    @GetMapping("/sendAtByAddWeibo/{wid}")
    public Object sendAtByAddWeibo(@PathVariable("wid") Integer wid) {
        messageService.sendAtByWid(wid, ServletUtil.getUid());
        return ResultUtil.success(null);
    }

    @DeleteMapping("/{mid}")
    public Object delete(@PathVariable("mid") Integer mid) {
        messageService.delete(mid);
        return ResultUtil.success(null);
    }

    @GetMapping("/badge")
    public Object badge(@RequestParam("atId") Integer atId, @RequestParam("commentId") Integer commentId,
                        @RequestParam("likeId") Integer likeId, @RequestParam("systemId") Integer systemId) {
        return ResultUtil.success(messageService.badge(atId, commentId, likeId, systemId));
    }
}
