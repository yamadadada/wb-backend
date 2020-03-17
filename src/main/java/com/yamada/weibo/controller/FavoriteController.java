package com.yamada.weibo.controller;

import com.yamada.weibo.service.FavoriteService;
import com.yamada.weibo.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{wid}")
    public Object add(@PathVariable("wid") Integer wid) {
        favoriteService.add(wid);
        return ResultUtil.success(null);
    }

    @DeleteMapping("/{wid}")
    public Object delete(@PathVariable("wid") Integer wid) {
        favoriteService.delete(wid);
        return ResultUtil.success(null);
    }
}
