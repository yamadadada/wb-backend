package com.yamada.weibo.service;

import com.yamada.weibo.vo.CommentVO;

import java.util.List;

public interface CommentService {

    CommentVO getByCid(Integer cid);

    List<CommentVO> getLevel2ByCid(Integer cid, String sort);
}
