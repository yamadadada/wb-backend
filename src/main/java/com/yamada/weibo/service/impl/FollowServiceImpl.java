package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.FollowMapper;
import com.yamada.weibo.pojo.Follow;
import com.yamada.weibo.service.FollowService;
import com.yamada.weibo.utils.ServletUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FollowServiceImpl implements FollowService {

    @Resource
    private FollowMapper followMapper;

    @Override
    public void addFollow(Integer uid) {
        Integer loginUid = ServletUtil.getUid();
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", loginUid);
        wrapper.eq("follow_uid", uid);
        Follow follow = followMapper.selectOne(wrapper);
        if (follow == null) {
            follow = new Follow();
            follow.setUid(loginUid);
            follow.setFollowUid(uid);
            int result = followMapper.insert(follow);
            if (result == 0) {
                throw new MyException(ResultEnum.OPERATE_ERROR);
            }
        }
    }

    @Override
    public void deleteFollow(Integer uid) {
        Integer loginUid = ServletUtil.getUid();
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", loginUid);
        wrapper.eq("follow_uid", uid);
        Follow follow = followMapper.selectOne(wrapper);
        if (follow != null) {
            int result = followMapper.delete(wrapper);
            if (result == 0) {
                throw new MyException(ResultEnum.OPERATE_ERROR);
            }
        }
    }
}
