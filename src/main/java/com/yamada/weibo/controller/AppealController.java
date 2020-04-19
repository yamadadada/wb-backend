package com.yamada.weibo.controller;

import com.yamada.weibo.pojo.Appeal;
import com.yamada.weibo.service.AppealService;
import com.yamada.weibo.utils.ResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appeal")
public class AppealController {

    private final AppealService appealService;

    @Autowired
    public AppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    @GetMapping("/{type}/{id}")
    public Object appeal(@PathVariable("id") Integer targetId, @PathVariable("type") Integer targetType,
                         @RequestParam("type") Integer appealType,
                         @RequestParam(value = "content", defaultValue = "") String content) {
        Appeal appeal = new Appeal();
        appeal.setTargetId(targetId);
        appeal.setTargetType(targetType);
        appeal.setAppealType(appealType);
        if (StringUtils.isNotBlank(content)) {
            appeal.setContent(content);
        }
        appealService.appeal(appeal);
        return ResultUtil.success(null);
    }
}
