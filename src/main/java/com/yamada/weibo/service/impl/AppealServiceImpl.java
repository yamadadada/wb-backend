package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yamada.weibo.enums.AppealStatus;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.enums.TargetType;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.AppealMapper;
import com.yamada.weibo.mapper.CommentMapper;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.mapper.WeiboMapper;
import com.yamada.weibo.pojo.Appeal;
import com.yamada.weibo.pojo.Comment;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.service.*;
import com.yamada.weibo.utils.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class AppealServiceImpl implements AppealService {

    @Resource
    private AppealMapper appealMapper;

    @Resource
    private WeiboMapper weiboMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserMapper userMapper;

    @Value("${appeal.weibo-limit}")
    private Integer weiboLimit;

    @Value("${appeal.comment-limit}")
    private Integer commentLimit;

    @Value("${appeal.user-limit}")
    private Integer userLimit;

    private final WeiboService weiboService;

    private final CommentService commentService;

    private final UserService userService;

    private final MessageService messageService;

    @Autowired
    public AppealServiceImpl(WeiboService weiboService, CommentService commentService, UserService userService,
                             MessageService messageService) {
        this.weiboService = weiboService;
        this.commentService = commentService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void appeal(Appeal appeal) {
        int loginUid = ServletUtil.getUid();
        Object target;
        // 检测target是否存在
        if (TargetType.WEIBO.getCode().equals(appeal.getTargetType())) {
            Weibo weibo = weiboMapper.selectById(appeal.getTargetId());
            if (weibo == null) {
                throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
            }
            target = weibo;
        } else if (TargetType.COMMENT.getCode().equals(appeal.getTargetType())){
            Comment comment = commentMapper.selectById(appeal.getTargetId());
            if (comment == null) {
                throw new MyException(ResultEnum.COMMENT_NOT_EXIST);
            }
            target = comment;
        } else {
            if (loginUid == appeal.getTargetId()) {
                // 不能举报自己
                throw new MyException(ResultEnum.OPERATE_ERROR);
            }
            User user = userMapper.selectById(appeal.getTargetId());
            if (user == null) {
                throw new MyException(ResultEnum.USER_NOT_EXIST);
            }
            target = user;
        }
        // 检测是否重复举报
        QueryWrapper<Appeal> wrapper = new QueryWrapper<>();
        wrapper.eq("target_id", appeal.getTargetId()).eq("target_type", appeal.getTargetType())
                .eq("uid", loginUid).select("aid");
        Appeal appeal1 = appealMapper.selectOne(wrapper);
        if (appeal1 != null) {
            throw new MyException(ResultEnum.CANNOT_APPEAL_REPEAT);
        }

        appeal.setUid(loginUid);
        appeal.setStatus(AppealStatus.APPEAL.getCode());
        int result = appealMapper.insert(appeal);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
        autoBan(appeal.getTargetId(), appeal.getTargetType(), target, loginUid);
    }

    @Async
    @Override
    public void autoBan(Integer targetId, Integer targetType, Object target, Integer loginUid) {
        QueryWrapper<Appeal> wrapper = new QueryWrapper<>();
        wrapper.eq("target_id", targetId).eq("target_type", targetType)
                .eq("status", AppealStatus.APPEAL.getCode());
        List<Appeal> appealList = appealMapper.selectList(wrapper);
        int count = appealList.size();
        if (TargetType.WEIBO.getCode().equals(targetType)) {
            // 微博
            if (count >= weiboLimit) {
                weiboService.delete(targetId);
                updateStatus(targetId, targetType, AppealStatus.DONE);
                // 发送消息
                String message = "你发布的微博因被多人投诉而被删除(" +
                        ((Weibo)target).getContent().replaceAll("\n", "") + ")";
                messageService.sendSystem(((Weibo)target).getUid(), message, -1);
                message = "你投诉的微博已被删除(" +
                        ((Weibo)target).getContent().replaceAll("\n", "") + ")";
                for (Appeal appeal : appealList) {
                    messageService.sendSystem(appeal.getUid(), message, -1);
                }
            }
        } else if (TargetType.COMMENT.getCode().equals(targetType)) {
            // 评论
            if (count >= commentLimit) {
                commentService.delete(targetId);
                updateStatus(targetId, targetType, AppealStatus.DONE);
                // 发送消息
                String message = "你发表的评论因被多人投诉而被删除(" +
                        ((Comment)target).getContent().replaceAll("\n", "") + ")";
                messageService.sendSystem(((Comment)target).getUid(), message, -1);
                message = "你投诉的评论已被删除(" +
                        ((Comment)target).getContent().replaceAll("\n", "") + ")";
                for (Appeal appeal : appealList) {
                    messageService.sendSystem(appeal.getUid(), message, -1);
                }
            }
        } else {
            // 用户
            if (count >= userLimit) {
                userService.ban(targetId);
                updateStatus(targetId, targetType, AppealStatus.DONE);
                // 发送消息
                String message = "你的帐号因被多人投诉而被封禁";
                messageService.sendSystem(targetId, message, -1);
                message = "你投诉的用户：" + ((User)target).getName() + " 已被封禁";
                for (Appeal appeal : appealList) {
                    messageService.sendSystem(appeal.getUid(), message, -1);
                }
            }
        }
    }

    private void updateStatus(Integer targetId, Integer targetType, AppealStatus status) {
        UpdateWrapper<Appeal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("target_id", targetId).eq("target_type", targetType);
        Appeal a = new Appeal();
        a.setStatus(status.getCode());
        int result = appealMapper.update(a, updateWrapper);
        if (result == 0) {
            log.error("【Appeal】更新状态失败, targetId: " + targetId + " targetType: " + targetType + " status: " + status.getCode());
        }
    }
}
