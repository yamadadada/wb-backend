package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.mapper.TopicMapper;
import com.yamada.weibo.mapper.WeiboTopicMapper;
import com.yamada.weibo.pojo.Topic;
import com.yamada.weibo.pojo.WeiboTopic;
import com.yamada.weibo.service.TopicService;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.utils.TextUtil;
import com.yamada.weibo.vo.TopicVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TopicServiceImpl implements TopicService {

    @Resource
    private TopicMapper topicMapper;

    @Resource
    private WeiboTopicMapper weiboTopicMapper;

    @Override
    public List<TopicVO> searchByName(String name) {
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        wrapper.like("name", name);
        List<Topic> topicList = topicMapper.selectList(wrapper);
        List<TopicVO> topicVOList = new ArrayList<>();
        for (Topic topic : topicList) {
            TopicVO topicVO = new TopicVO();
            BeanUtils.copyProperties(topic, topicVO);
            topicVOList.add(topicVO);
        }
        return topicVOList;
    }

    @Override
    @Async
    public void addByWeibo(Integer wid, String content, Integer loginUid) {
        List<String> topicList = TextUtil.parseTopic(content);
        for (String name : topicList) {
            QueryWrapper<Topic> wrapper = new QueryWrapper<>();
            wrapper.eq("name", name);
            Topic topic = topicMapper.selectOne(wrapper);
            if (topic == null) {
                topic = new Topic();
                topic.setName(name);
                topic.setUid(loginUid);
                int result = topicMapper.insert(topic);
                if (result == 0) {
                    log.error("【Topic】插入失败：" + name);
                }
            }
            if (topic.getTid() == null) {
                log.error("【Topic】Tid不存在，name=" + name);
                continue;
            }
            WeiboTopic weiboTopic = new WeiboTopic();
            weiboTopic.setWid(wid);
            weiboTopic.setTid(topic.getTid());
            int result = weiboTopicMapper.insert(weiboTopic);
            if (result == 0) {
                log.error("【WeiboTopic】插入失败, wid=" + wid + ", tid=" + topic.getTid());
            }
        }
    }
}
