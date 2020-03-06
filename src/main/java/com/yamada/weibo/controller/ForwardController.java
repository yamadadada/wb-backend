package com.yamada.weibo.controller;

import com.yamada.weibo.form.ForwardForm;
import com.yamada.weibo.service.ForwardService;
import com.yamada.weibo.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forward")
public class ForwardController {

    private final ForwardService forwardService;

    @Autowired
    public ForwardController(ForwardService forwardService) {
        this.forwardService = forwardService;
    }

    @PostMapping("")
    public Object add(@RequestBody @Validated ForwardForm form) {
        forwardService.add(form);
        return ResultUtil.success(null);
    }
}
