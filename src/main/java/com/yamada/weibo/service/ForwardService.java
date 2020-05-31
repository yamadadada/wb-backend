package com.yamada.weibo.service;

import com.yamada.weibo.form.ForwardForm;
import com.yamada.weibo.vo.WeiboVO;

public interface ForwardService {

    void add(ForwardForm form);

    WeiboVO baseInfo(Integer wid);
}
