package com.yamada.weibo.service;

import com.yamada.weibo.vo.TopicVO;

import java.util.List;

public interface TopicService {

    List<TopicVO> searchByName(String name);

    void addByWeibo(Integer wid, String content, Integer loginUid);
}
