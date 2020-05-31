package com.yamada.weibo.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.form.UserInfoForm;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.service.AuthService;
import com.yamada.weibo.utils.JwtUtil;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.vo.LoginResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    @Resource
    private UserMapper userMapper;

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> login(String code) {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "https://api.weixin.qq.com/sns/jscode2session";
        String url = baseUrl + "?appid=" + appid + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        String json = restTemplate.getForObject(url, String.class);
        // 自己解析JSON数据
        LoginResponse response = JSON.parseObject(json, LoginResponse.class);
        if (response == null || response.getErrcode() != null) {
            throw new MyException(ResultEnum.BUSY);
        }

        String openid = response.getOpenid();
        String sessionKey = response.getSessionKey();
        // 判断这个openid是否已存在，选择注册/登录操作
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("openid", openid);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            // 注册
            user = new User();
            user.setOpenid(openid);
            user.setSessionKey(sessionKey);
            // 生成一个不重复的随机呢称
            String name = getRandomName();
            wrapper = new QueryWrapper<>();
            wrapper.eq("name", name);
            while (userMapper.selectOne(wrapper) != null) {
                // 名字重复，继续循环
                name = getRandomName();
                wrapper.eq("name", name);
            }
            user.setName(name);
            int result = userMapper.insert(user);
            if (result == 0) {
                throw new MyException(ResultEnum.OPERATE_ERROR);
            }
        }
        // 生成token
        String token = jwtUtil.create(user);
        Map<String, Object> map = new HashMap<>();
        map.put("uid", user.getUid());
        if (StringUtils.isNotBlank(user.getSchool())) {
            map.put("school", user.getSchool());
        }
        if (StringUtils.isNotBlank(user.getLocation())) {
            map.put("city", user.getLocation());
        }
        map.put("token", token);
        return map;
    }

    @Override
    public Map<String, Object> getUserInfo(UserInfoForm userInfoForm) {
        Integer uid = ServletUtil.getUid();
        Map<String, Object> map = new HashMap<>();
        // 验证name是否重复
        String name = userInfoForm.getName();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        User tempUser = userMapper.selectOne(wrapper);
        if (tempUser != null && !tempUser.getUid().equals(uid)) {
            map.put("message", "当前昵称已被占用，请前往个人中心修改昵称");
            userInfoForm.setName(null);
        }
        User user = new User();
        user.setUid(uid);
        BeanUtils.copyProperties(userInfoForm, user);
        // 解析地区
        String location = userInfoForm.getCountry();
        if ("中国".equals(userInfoForm.getCountry())) {
            location = userInfoForm.getProvince() + " " + userInfoForm.getCity();
        }
        user.setLocation(location);
        int result = userMapper.updateById(user);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
        map.put("city", location);
        return map;
    }

    private String getRandomName() {
        return "用户_" + (10000000 + new Random().nextInt(10000000));
    }
}
