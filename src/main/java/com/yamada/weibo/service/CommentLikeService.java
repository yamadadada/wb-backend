package com.yamada.weibo.service;

public interface CommentLikeService {

    void like(Integer cid);

    void notLike(Integer cid);
}
