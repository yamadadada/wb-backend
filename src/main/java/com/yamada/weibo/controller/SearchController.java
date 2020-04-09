package com.yamada.weibo.controller;

import com.yamada.weibo.service.SearchService;
import com.yamada.weibo.utils.ResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/hotSearch")
    public Object hotSearch() {
        return ResultUtil.success(searchService.hotSearch());
    }

    @GetMapping("/candidate")
    public Object candidate(@RequestParam("content") String content) {
        if (StringUtils.isBlank(content)) {
            return ResultUtil.success(new ArrayList<>());
        }
        return ResultUtil.success(searchService.candidate(content));
    }
}
