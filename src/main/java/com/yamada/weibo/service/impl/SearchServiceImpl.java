package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yamada.weibo.mapper.TopicMapper;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.pojo.Topic;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private TopicMapper topicMapper;

    @Resource
    private UserMapper userMapper;

    @Value("${hot.search.score}")
    private Double searchScore;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public SearchServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Async
    public void addSearch(String content) {
        redisTemplate.opsForZSet().incrementScore("hot-list", content, searchScore);
    }

    @Override
    public Set<String> hotSearch() {
        return redisTemplate.opsForZSet().reverseRange("hot-list", 0, 50);
    }

    @Override
    public Set<String> candidate(String content) {
        if (content.startsWith("#")) {
            content = content.substring(1);
            if (content.endsWith("#")) {
                content = content.substring(0, content.length() - 1);
            }
            QueryWrapper<Topic> wrapper1 = new QueryWrapper<>();
            wrapper1.like("name", content);
            PageHelper.startPage(0, 10);
            return topicMapper.selectList(wrapper1).stream().map(e -> "#" + e.getName() + "#").collect(Collectors.toSet());
        }
        // 从话题中获取
        QueryWrapper<Topic> wrapper1 = new QueryWrapper<>();
        wrapper1.like("name", content);
        PageHelper.startPage(0, 10);
        List<Topic> topicList = topicMapper.selectList(wrapper1);
        Set<String> result = topicList.stream().map(e -> "#" + e.getName() + "#").collect(Collectors.toSet());
        // 从用户名称中获取
        QueryWrapper<User> wrapper2 = new QueryWrapper<>();
        wrapper2.like("name", content);
        PageHelper.startPage(0, 10);
        List<User> userList = userMapper.selectList(wrapper2);
        result.addAll(userList.stream().map(User::getName).collect(Collectors.toSet()));
        return result;
    }
}
