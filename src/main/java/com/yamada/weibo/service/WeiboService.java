package com.yamada.weibo.service;

import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.vo.WeiboLikeVO;
import com.yamada.weibo.vo.WeiboVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface WeiboService {

    List<WeiboVO> getFollowWeibo(Integer page, Integer size);

    WeiboVO getWeiboDetail(Integer wid);

    List<WeiboVO> getWeiboForward(Integer wid);

    Map<String, Object> getWeiboComment(Integer wid, String sort);

    List<WeiboLikeVO> getWeiboLike(Integer wid);

    Integer add(Weibo weibo);

    void delete(Integer wid);

    void upload(Integer wid, MultipartFile file);

    List<WeiboVO> getByUid(Integer uid);

    List<WeiboVO> myLike();

    List<WeiboVO> myFavorite();

    List<WeiboVO> realTime();

    List<WeiboVO> shcool();
}
