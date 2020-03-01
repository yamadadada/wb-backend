package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.WeiboLikeMapper;
import com.yamada.weibo.mapper.WeiboMapper;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.pojo.WeiboLike;
import com.yamada.weibo.service.WeiboLikeService;
import com.yamada.weibo.utils.ServletUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WeiboLikeServiceImpl implements WeiboLikeService {

    @Resource
    private WeiboLikeMapper weiboLikeMapper;

    @Resource
    private WeiboMapper weiboMapper;

    @Override
    public void like(Integer wid) {
        if (weiboMapper.selectById(wid) == null) {
            throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
        }
        Integer uid = ServletUtil.getUid();
        QueryWrapper<WeiboLike> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("wid", wid);
        if (weiboLikeMapper.selectOne(wrapper) == null) {
            WeiboLike weiboLike = new WeiboLike();
            weiboLike.setUid(uid);
            weiboLike.setWid(wid);
            weiboLikeMapper.insert(weiboLike);
        }
    }

    @Override
    public void notLike(Integer wid) {
        Integer uid = ServletUtil.getUid();
        if (weiboMapper.selectById(wid) == null) {
            throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
        }
        QueryWrapper<WeiboLike> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("wid", wid);
        if (weiboLikeMapper.selectOne(wrapper) != null) {
            weiboLikeMapper.delete(wrapper);
        }
    }
}
