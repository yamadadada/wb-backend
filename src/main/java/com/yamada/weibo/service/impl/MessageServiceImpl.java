package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.CommentType;
import com.yamada.weibo.enums.MessageType;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.enums.WeiboStatus;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.CommentMapper;
import com.yamada.weibo.mapper.MessageMapper;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.mapper.WeiboMapper;
import com.yamada.weibo.pojo.Comment;
import com.yamada.weibo.pojo.Message;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.service.MessageService;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.utils.TextUtil;
import com.yamada.weibo.vo.BadgeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.StringJoiner;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private WeiboMapper weiboMapper;

    @Resource
    private CommentMapper commentMapper;

    @Override
    @Async
    public void sendAt(Weibo weibo, Integer loginUid) {
        List<String> nameList = TextUtil.parseName(weibo.getContent());
        if (nameList.size() == 0) {
            return;
        }
        List<User> userList = getUserList(nameList);
        User loginUser = userMapper.selectById(loginUid);
        String baseInfo = getBaseByWeibo(weibo);
        for (User user : userList) {
            Message message = new Message();
            message.setUid(user.getUid());
            message.setSendUid(loginUid);
            message.setSendName(loginUser.getName());
            message.setSendAvatar(loginUser.getAvatar());
            message.setContent(weibo.getContent());
            if (baseInfo != null) {
                if (baseInfo.startsWith("image")) {
                    message.setImage(baseInfo.split("image:")[1]);
                } else {
                    message.setBaseContent(baseInfo.substring(baseInfo.indexOf(":") + 1));
                }
            }
            message.setWid(weibo.getWid());
            message.setType(MessageType.AT.getCode());
            int result = messageMapper.insert(message);
            if (result == 0) {
                log.error("【发送消息】@消息发送失败：" + message.toString());
            }
        }
    }

    @Override
    @Async
    public void sendAt(Comment comment, Integer loginUid) {
        List<String> nameList = TextUtil.parseName(comment.getContent());
        if (nameList.size() == 0) {
            return;
        }
        User loginUser = userMapper.selectById(loginUid);
        List<User> userList = getUserList(nameList);
        String baseInfo = getBaseByComment(comment);
        for (User user : userList) {
            Message message = new Message();
            message.setUid(user.getUid());
            message.setSendUid(loginUser.getUid());
            message.setSendName(loginUser.getName());
            message.setSendAvatar(loginUser.getAvatar());
            message.setContent(comment.getContent());
            if (baseInfo != null && baseInfo.startsWith("image")) {
                message.setImage(baseInfo.split("image:")[1]);
            } else if (baseInfo != null && baseInfo.startsWith("content:")){
                message.setBaseContent(baseInfo.substring(baseInfo.indexOf(":") + 1));
            }
            message.setWid(comment.getWid());
            message.setType(MessageType.AT.getCode());
            int result = messageMapper.insert(message);
            if (result == 0) {
                log.error("【发送消息】@消息发送失败：" + message.toString());
            }
        }
    }

    @Override
    public void sendAtByWid(Integer wid, Integer loginUid) {
        Weibo weibo = weiboMapper.selectById(wid);
        if (weibo == null) {
            return;
        }
        sendAt(weibo, loginUid);
    }

    @Override
    @Async
    public void sendComment(Comment comment, Integer loginUid) {
        Message message = new Message();
        User loginUser = userMapper.selectById(loginUid);
        message.setSendUid(loginUser.getUid());
        message.setSendName(loginUser.getName());
        message.setSendAvatar(loginUser.getAvatar());
        message.setContent(comment.getContent());
        message.setType(MessageType.COMMENT.getCode());
        if (CommentType.LEVEL1.getCode().equals(comment.getCommentType())) {
            // 第一层评论
            Weibo weibo = weiboMapper.selectById(comment.getWid());
            if (weibo == null) {
                return;
            }
            message.setUid(weibo.getUid());
            if (weibo.getImages() != null) {
                message.setImage(weibo.getImages().split(",")[0]);
            } else {
                message.setBaseContent(weibo.getContent());
            }
            message.setWid(weibo.getWid());
        } else {
            // 第二层评论
            Comment comment1 = commentMapper.selectById(comment.getCommentCid());
            if (comment1 == null) {
                return;
            }
            message.setUid(comment1.getUid());
            message.setBaseContent(comment1.getContent());
            message.setCid(comment1.getCid());
            if (comment1.getCommentUid() != null) {
                Message message1 = new Message();
                BeanUtils.copyProperties(message, message1);
                message1.setUid(comment1.getCommentUid());
                int result = messageMapper.insert(message1);
                if (result == 0) {
                    log.error("【发送消息】评论消息发送失败：" + message1.toString());
                }
            }
        }
        int result = messageMapper.insert(message);
        if (result == 0) {
            log.error("【发送消息】评论消息发送失败：" + message.toString());
        }
    }

    @Override
    @Async
    public void sendLikeByWid(Integer wid, Integer loginUid) {
        Weibo weibo = weiboMapper.selectById(wid);
        if (weibo == null) {
            return;
        }
        Message message = new Message();
        message.setUid(weibo.getUid());
        User loginUser = userMapper.selectById(loginUid);
        message.setSendUid(loginUser.getUid());
        message.setSendName(loginUser.getName());
        message.setSendAvatar(loginUser.getAvatar());
        if (weibo.getImages() != null) {
            message.setImage(weibo.getImages().split(",")[0]);
        } else {
            message.setBaseContent(weibo.getContent());
        }
        if (StringUtils.isBlank(loginUser.getIntroduction())) {
            message.setContent("");
        } else {
            message.setContent(loginUser.getIntroduction());
        }
        message.setWid(weibo.getWid());
        message.setType(MessageType.LIKE.getCode());
        int result = messageMapper.insert(message);
        if (result == 0) {
            log.error("【发送消息】点赞消息发送失败：" + message.toString());
        }
    }

    @Override
    @Async
    public void sendLikeByCid(Integer cid, Integer loginUid) {
        Message message = new Message();
        Comment comment = commentMapper.selectById(cid);
        if (comment == null) {
            return;
        }
        message.setUid(comment.getUid());
        User loginUser = userMapper.selectById(loginUid);
        message.setSendUid(loginUser.getUid());
        message.setSendName(loginUser.getName());
        message.setSendAvatar(loginUser.getAvatar());
        message.setBaseContent(comment.getContent());
        if (CommentType.LEVEL1.getCode().equals(comment.getCommentType())) {
            message.setWid(comment.getWid());
        } else {
            message.setCid(comment.getCommentCid());
        }
        if (StringUtils.isNotBlank(loginUser.getIntroduction())) {
            message.setContent(loginUser.getIntroduction());
        } else {
            message.setContent("");
        }
        message.setType(MessageType.LIKE.getCode());
        int result = messageMapper.insert(message);
        if (result == 0) {
            log.error("【发送消息】点赞消息发送失败：" + message.toString());
        }
    }

    @Override
    @Async
    public void sendSystem(Integer uid, String content, Integer loginUid) {
        User loginUser = userMapper.selectById(loginUid);
        Message message = new Message();
        message.setUid(uid);
        message.setSendUid(loginUser.getUid());
        message.setSendName(loginUser.getName());
        message.setSendAvatar(loginUser.getAvatar());
        message.setContent(content);
        message.setType(MessageType.SYSTEM.getCode());
        int result = messageMapper.insert(message);
        if (result == 0) {
            log.error("【发送消息】系统消息发送失败：" + message.toString());
        }
    }

    @Override
    public List<Message> getList(MessageType type) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", ServletUtil.getUid()).orderByDesc("create_time");
        if (MessageType.AT == type) {
            wrapper.eq("type", MessageType.AT.getCode());
        } else if (MessageType.COMMENT == type) {
            wrapper.eq("type", MessageType.COMMENT.getCode());
        } else if (MessageType.LIKE == type){
            wrapper.eq("type", MessageType.LIKE.getCode());
        } else {
            wrapper.eq("type", MessageType.SYSTEM.getCode());
        }
        return messageMapper.selectList(wrapper);
    }

    @Override
    public void delete(Integer mid) {
        Message message = messageMapper.selectById(mid);
        if (message == null) {
            throw new MyException(ResultEnum.MESSAGE_NOT_EXIST);
        }
        int uid = ServletUtil.getUid();
        if (uid != message.getUid()) {
            throw new MyException(ResultEnum.NOT_AUTH);
        }
        int result = messageMapper.deleteById(mid);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
    }

    @Override
    public BadgeVO badge(Integer atId, Integer commentId, Integer likeId, Integer systemId) {
        int loginUid = ServletUtil.getUid();
        int min = Math.min(atId, Math.min(commentId, Math.min(likeId, systemId)));
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", loginUid).gt("mid", min).orderByDesc("create_time");
        List<Message> messageList = messageMapper.selectList(wrapper);
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        int count4 = 0;
        for (Message message : messageList) {
            if (MessageType.AT.getCode().equals(message.getType()) && message.getMid() > atId) {
                count1++;
            } else if (MessageType.COMMENT.getCode().equals(message.getType()) && message.getMid() > commentId) {
                count2++;
            } else if (MessageType.LIKE.getCode().equals(message.getType()) && message.getMid() > likeId) {
                count3++;
            } else if (MessageType.SYSTEM.getCode().equals(message.getType()) && message.getMid() > systemId){
                count4++;
            }
        }
        return new BadgeVO(count1, count2, count3, count4, count1 + count2 + count3 + count4);
    }

    private List<User> getUserList(List<String> nameList) {
        StringJoiner sj = new StringJoiner(",");
        for (String name : nameList) {
            sj.add(name);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("name", sj.toString());
        return userMapper.selectList(wrapper);
    }

    private String getBaseByWeibo(Weibo weibo) {
        if (WeiboStatus.FORMAL.getCode().equals(weibo.getStatus()) && weibo.getImages() != null) {
            return "image:" + weibo.getImages().split(",")[0];
        }
        if (WeiboStatus.FORWARD.getCode().equals(weibo.getStatus())) {
            // 转发
            Weibo forwardWeibo = weiboMapper.selectById(weibo.getBaseForwardWid());
            if (forwardWeibo != null) {
                if (forwardWeibo.getImages() != null) {
                    return "image:" + forwardWeibo.getImages().split(",")[0];
                } else {
                    return "content:" + forwardWeibo.getContent();
                }
            }
        }
        return null;
    }

    private String getBaseByComment(Comment comment) {
        if (CommentType.LEVEL1.getCode().equals(comment.getCommentType())) {
            // 第一层评论
            Weibo weibo = weiboMapper.selectById(comment.getWid());
            if (weibo != null) {
                if (weibo.getImages() != null) {
                    return "image:" + weibo.getImages().split(",")[0];
                } else {
                    return "content:" + weibo.getContent();
                }
            }
        } else {
            // 第二层评论
            Comment comment1 = commentMapper.selectById(comment.getCommentCid());
            if (comment1 != null) {
                return "content:" + comment1.getContent();
            }
        }
        return null;
    }
}
