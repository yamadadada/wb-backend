package com.yamada.weibo.controller;

import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.form.ForwardForm;
import com.yamada.weibo.service.ForwardService;
import com.yamada.weibo.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
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
    public Object add(@RequestBody @Validated ForwardForm form, BindingResult result) {
        if (result.hasErrors()) {
            throw new MyException(result.getAllErrors().get(0).getDefaultMessage());
        }
        forwardService.add(form);
        return ResultUtil.success(null);
    }

    /**
     * 获得转发的微博的基本信息
     * @param wid
     * @return
     */
    @GetMapping("/baseInfo/{wid}")
    public Object baseInfo(@PathVariable("wid") Integer wid) {
        return ResultUtil.success(forwardService.baseInfo(wid));
    }
}
