package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.enums.UserStatus;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.FollowMapper;
import com.yamada.weibo.mapper.SchoolMapper;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.mapper.WeiboMapper;
import com.yamada.weibo.pojo.Follow;
import com.yamada.weibo.pojo.School;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.service.UserService;
import com.yamada.weibo.utils.FileUtil;
import com.yamada.weibo.utils.PinyinUtil;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.vo.UserIndexVO;
import com.yamada.weibo.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private WeiboMapper weiboMapper;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private SchoolMapper schoolMapper;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public UserServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserVO getUserInfo(Integer uid) {
        User user = userMapper.selectById(uid);
        return toUserVO(user);
    }

    @Override
    public void update(Integer uid, User user) {
        Integer loginUid = ServletUtil.getUid();
        if (!loginUid.equals(uid)) {
            throw new MyException(ResultEnum.NOT_AUTH);
        }
        // 判断昵称重复
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", user.getName());
        User u = userMapper.selectOne(wrapper);
        if (u != null && !u.getUid().equals(uid)) {
            throw new MyException(ResultEnum.NAME_REPETITION);
        }
        if (u == null) {
            // 修改了昵称
            // 检查是否在间隔时间
            String s = redisTemplate.opsForValue().get("user::name:interval::" + uid);
            if (s != null) {
                throw new MyException(ResultEnum.NAME_INTERVAL);
            }
            // 添加间隔时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            redisTemplate.opsForValue()
                    .set("user::name:interval::" + uid, sdf.format(new Date()), 30, TimeUnit.DAYS);
        }
        user.setUid(uid);
        int result = userMapper.updateById(user);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
    }

    @Override
    public List<UserVO> getSimpleUserVO(String column) {
        Integer loginUid = ServletUtil.getUid();
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq(column, loginUid).orderByDesc("follow_time");
        List<Follow> followList = followMapper.selectList(wrapper);
        if (followList.size() == 0) {
            return new ArrayList<>();
        }
        List<Integer> uidList = followList.stream().map(Follow::getFollowUid).collect(Collectors.toList());
        List<User> userList = userMapper.selectBatchIds(uidList);
        List<UserVO> userVOList = new ArrayList<>();
        for (User user : userList) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVOList.add(userVO);
        }
        return userVOList;
    }

    @Override
    public void updateAvatar(Integer uid, MultipartFile file) {
        Integer loginUid = ServletUtil.getUid();
        if (!loginUid.equals(uid)) {
            throw new MyException(ResultEnum.NOT_AUTH);
        }
        User user = userMapper.selectById(uid);
        String fileName =  uid + "_" + System.currentTimeMillis() + ".png";
        if (FileUtil.upload(file, FileUtil.imagePath, fileName)) {
            if (user.getAvatar() != null) {
                // 如果是本地头像，删除
                if (user.getAvatar().startsWith(FileUtil.imageHost)) {
                    String deleteFile = user.getAvatar().substring(user.getAvatar().lastIndexOf("/") + 1);
                    FileUtil.delete(FileUtil.imagePath, deleteFile);
                }
            }
            String fullPath = FileUtil.imageHost + "/images/" + fileName;
            User user1 = new User();
            user1.setUid(uid);
            user1.setAvatar(fullPath);
            int result = userMapper.updateById(user1);
            if (result == 0) {
                throw new MyException(ResultEnum.OPERATE_ERROR);
            }
        }
    }

    @Override
    public List<UserVO> searchByName(String name) {
        List<UserVO> userVOList = new ArrayList<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("name", name);
        List<User> userList = userMapper.selectList(wrapper);
        for (User user : userList) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVOList.add(userVO);
        }
        return userVOList;
    }

    @Override
    public List<UserIndexVO> getUserIndex() {
        // 获得所有的关注用户和粉丝
        Integer uid = ServletUtil.getUid();
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).or().eq("follow_uid", uid);
        List<Follow> followList = followMapper.selectList(wrapper);
        HashSet<Integer> uidSet = new HashSet<>();
        for (Follow follow : followList) {
            if (!follow.getUid().equals(uid)) {
                uidSet.add(follow.getUid());
            } else if (!follow.getFollowUid().equals(uid)) {
                uidSet.add(follow.getFollowUid());
            }
        }
        if (uidSet.size() == 0) {
            return new ArrayList<>();
        }
        List<User> userList = userMapper.selectBatchIds(uidSet);
        // 按首字母排序
        List<UserIndexVO> result = new ArrayList<>();
        userList.sort((o1, o2) -> PinyinUtil.sortByPinyin(o1.getName(), o2.getName()));
        String tempLetter = null;
        for (User user : userList) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            String firstLetter = PinyinUtil.getFirstLetter(userVO.getName());
            if (tempLetter == null || !tempLetter.equals(firstLetter)) {
                tempLetter = firstLetter;
                UserIndexVO userIndexVO = new UserIndexVO(tempLetter);
                userIndexVO.add(userVO);
                result.add(userIndexVO);
            } else {
                result.get(result.size() - 1).add(userVO);
            }
        }
        return result;
    }

    @Override
    public UserVO getByName(String name) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        User user = userMapper.selectOne(wrapper);
        return toUserVO(user);
    }

    @Override
    public void ban(Integer uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new MyException(ResultEnum.USER_NOT_EXIST);
        }
        user.setStatus(UserStatus.BAN.getCode());
        int result = userMapper.updateById(user);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
    }

    @Override
    public List<String> searchSchool(String school) {
        QueryWrapper<School> wrapper = new QueryWrapper<>();
        wrapper.select("name").likeRight("name", school);
        List<School> schoolList = schoolMapper.selectList(wrapper);
        return schoolList.stream().map(School::getName).collect(Collectors.toList());
    }

    private UserVO toUserVO(User user) {
        if (user == null) {
            throw new MyException(ResultEnum.USER_NOT_EXIST);
        }
        Integer uid = user.getUid();
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 解析注册时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        userVO.setRegisterTime(sdf.format(user.getCreateTime()));
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
        boolean isFollow = false;
        int loginUid = ServletUtil.getUid();
        for (Follow follow : followList) {
            if (follow.getUid().equals(uid)) {
                followCount++;
            } else {
                if (follow.getUid().equals(loginUid)) {
                    isFollow = true;
                }
                fanCount++;
            }
        }
        userVO.setIsFollow(isFollow);
        userVO.setFollowCount(followCount);
        userVO.setFanCount(fanCount);

        return userVO;
    }
}
