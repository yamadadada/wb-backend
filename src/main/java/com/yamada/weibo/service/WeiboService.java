package com.yamada.weibo.service;

import com.yamada.weibo.vo.WeiboLikeVO;
import com.yamada.weibo.vo.WeiboVO;

import java.util.List;
import java.util.Map;

public interface WeiboService {

    List<WeiboVO> getFollowWeibo(Integer page, Integer size);

    WeiboVO getWeiboDetail(Integer wid);

    List<WeiboVO> getWeiboForward(Integer wid);

    Map<String, Object> getWeiboComment(Integer wid, String sort);

    List<WeiboLikeVO> getWeiboLike(Integer wid);

    void delete(Integer wid);
}
