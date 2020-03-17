package com.yamada.weibo.service;

public interface FollowService {

    void addFollow(Integer uid);

    void deleteFollow(Integer uid);
}
