package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.FavoriteMapper;
import com.yamada.weibo.mapper.WeiboMapper;
import com.yamada.weibo.pojo.Favorite;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.service.FavoriteService;
import com.yamada.weibo.utils.ServletUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private WeiboMapper weiboMapper;

    @Override
    public void add(Integer wid) {
        int uid = ServletUtil.getUid();
        // 判断微博是否存在
        Weibo weibo = weiboMapper.selectById(wid);
        if (weibo == null) {
            throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
        }
        Favorite favorite = new Favorite();
        favorite.setUid(uid);
        favorite.setWid(wid);
        int result = favoriteMapper.insert(favorite);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
    }

    @Override
    public void delete(Integer wid) {
        int uid = ServletUtil.getUid();
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("wid", wid);
        int result = favoriteMapper.delete(wrapper);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
    }
}
