package com.yamada.weibo.service.impl;

import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.enums.UserStatus;
import com.yamada.weibo.enums.WeiboOperationType;
import com.yamada.weibo.enums.WeiboStatus;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.form.ForwardForm;
import com.yamada.weibo.mapper.ForwardMapper;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.mapper.WeiboMapper;
import com.yamada.weibo.pojo.Forward;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.service.ForwardService;
import com.yamada.weibo.service.MessageService;
import com.yamada.weibo.service.TopicService;
import com.yamada.weibo.service.WeiboService;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.vo.WeiboVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;

@Service
public class ForwardServiceImpl implements ForwardService {

    @Resource
    private ForwardMapper forwardMapper;

    @Resource
    private WeiboMapper weiboMapper;

    @Resource
    private UserMapper userMapper;

    private final TopicService topicService;

    private final WeiboService weiboService;

    private final MessageService messageService;

    @Autowired
    public ForwardServiceImpl(TopicService topicService, WeiboService weiboService, MessageService messageService) {
        this.topicService = topicService;
        this.weiboService = weiboService;
        this.messageService = messageService;
    }

    @Override
    @Transactional
    public void add(ForwardForm form) {
        Integer uid = ServletUtil.getUid();
        // 检查用户是否已被封禁
        User user = userMapper.selectById(uid);
        if (UserStatus.BAN.getCode().equals(user.getStatus())) {
            throw new MyException(ResultEnum.USER_BAN);
        }
        Weibo forwardWeibo = weiboMapper.selectById(form.getWid());
        if (forwardWeibo == null) {
            throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
        }
        // 封装信息
        Weibo weibo = new Weibo();
        weibo.setContent(form.getContent());
        weibo.setUid(uid);
        weibo.setStatus(WeiboStatus.FORWARD.getCode());

        if (forwardWeibo.getBaseForwardWid() == null) {
            // 第一层转发
            weibo.setBaseForwardWid(forwardWeibo.getWid());
        } else {
            // 非第一层转发
            weibo.setBaseForwardWid(forwardWeibo.getBaseForwardWid());
            String forwardLink = forwardWeibo.getForwardLink();
            if (forwardLink == null) {
                weibo.setForwardLink(String.valueOf(forwardWeibo.getWid()));
            } else {
                weibo.setForwardLink(forwardLink + "," + forwardWeibo.getWid());
            }
        }

        int result = weiboMapper.insert(weibo);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
        // 发送消息
        messageService.sendAt(weibo, ServletUtil.getUid());
        // 添加到forward表
        Forward forward = new Forward();
        forward.setWid(weibo.getWid());
        forward.setForwardWid(weibo.getBaseForwardWid());
        result = forwardMapper.insert(forward);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
        // 添加热度分数
        weiboService.increaseScore(forward.getForwardWid(), WeiboOperationType.FORWARD);
        if (weibo.getForwardLink() != null) {
            // 非第一层转发，逐层添加转发信息
            String[] split = weibo.getForwardLink().split(",");
            for (String s : split) {
                forward = new Forward();
                forward.setWid(weibo.getWid());
                forward.setForwardWid(Integer.valueOf(s));
                forwardMapper.insert(forward);
                // 添加热度分数
                weiboService.increaseScore(forward.getForwardWid(), WeiboOperationType.FORWARD);
            }
        }
        // 添加话题
        topicService.addByWeibo(weibo.getWid(), weibo.getContent(), ServletUtil.getUid());
    }

    @Override
    public WeiboVO baseInfo(Integer wid) {
        Weibo weibo = weiboMapper.selectById(wid);
        if (weibo == null) {
            throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
        }
        if (weibo.getBaseForwardWid() != null) {
            weibo = weiboMapper.selectById(weibo.getBaseForwardWid());
            if (weibo == null) {
                return null;
            }
        }
        WeiboVO weiboVO = new WeiboVO();
        BeanUtils.copyProperties(weibo, weiboVO);
        weiboVO.setContentString(weibo.getContent());
        User user = userMapper.selectById(weibo.getUid());
        weiboVO.setName(user.getName());
        if (weibo.getImages() != null) {
            // 使用第一张图片作为图片
            String[] split = weibo.getImages().split(",");
            weiboVO.setAvatar(split[0]);
        } else {
            // 使用头像作为图片
            weiboVO.setAvatar(user.getAvatar());
        }
        return weiboVO;
    }
}
