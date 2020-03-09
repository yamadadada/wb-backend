package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.FollowMapper;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.mapper.WeiboMapper;
import com.yamada.weibo.pojo.Follow;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.service.UserService;
import com.yamada.weibo.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private WeiboMapper weiboMapper;

    @Resource
    private FollowMapper followMapper;

    @Override
    public UserVO getUserInfo(Integer uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new MyException(ResultEnum.USER_NOT_EXIST);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 获得微博数
        QueryWrapper<Weibo> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("uid", uid);
        userVO.setWeiboCount(weiboMapper.selectCount(wrapper1));
        // 获得关注数和粉丝数
        int followCount = 0;
        int fanCount = 0;
        QueryWrapper<Follow> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("uid", uid).or().eq("follow_uid", uid);
        List<Follow> followList = followMapper.selectList(wrapper2);
        for (Follow follow : followList) {
            if (follow.getUid().equals(uid)) {
                followCount++;
            } else {
                fanCount++;
            }
        }
        userVO.setFollowCount(followCount);
        userVO.setFanCount(fanCount);
        return userVO;
    }
}
