package com.yamada.weibo.service;

public interface WeiboLikeService {

    void like(Integer wid);

    void notLike(Integer wid);
}
