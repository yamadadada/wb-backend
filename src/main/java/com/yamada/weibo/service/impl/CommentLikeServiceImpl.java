package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.CommentLikeMapper;
import com.yamada.weibo.mapper.CommentMapper;
import com.yamada.weibo.pojo.CommentLike;
import com.yamada.weibo.service.CommentLikeService;
import com.yamada.weibo.utils.ServletUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CommentLikeServiceImpl implements CommentLikeService {

    @Resource
    private CommentLikeMapper commentLikeMapper;

    @Resource
    private CommentMapper commentMapper;

    @Override
    public void like(Integer cid) {
        if (commentMapper.selectById(cid) == null) {
            throw new MyException(ResultEnum.COMMENT_NOT_EXIST);
        }
        Integer uid = ServletUtil.getUid();
        QueryWrapper<CommentLike> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("cid", cid);
        if (commentLikeMapper.selectOne(wrapper) == null) {
            CommentLike commentLike = new CommentLike();
            commentLike.setUid(uid);
            commentLike.setCid(cid);
            commentLikeMapper.insert(commentLike);
        }
    }

    @Override
    public void notLike(Integer cid) {
        if (commentMapper.selectById(cid) == null) {
            throw new MyException(ResultEnum.COMMENT_NOT_EXIST);
        }
        QueryWrapper<CommentLike> wrapper = new QueryWrapper<>();
        Integer uid = ServletUtil.getUid();
        wrapper.eq("uid", uid).eq("cid", cid);
        if (commentLikeMapper.selectOne(wrapper) != null) {
            commentLikeMapper.delete(wrapper);
        }
    }
}
