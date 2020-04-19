package com.yamada.weibo.service;

import com.yamada.weibo.pojo.Appeal;

public interface AppealService {

    void appeal(Appeal appeal);

    void autoBan(Integer targetId, Integer targetType, Object target, Integer loginUid);
}
