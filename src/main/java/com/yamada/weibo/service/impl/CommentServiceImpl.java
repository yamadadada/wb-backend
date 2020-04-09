package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.CommentType;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.enums.WeiboOperationType;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.CommentLikeMapper;
import com.yamada.weibo.mapper.CommentMapper;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.pojo.Comment;
import com.yamada.weibo.pojo.CommentLike;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.service.CommentService;
import com.yamada.weibo.service.MessageService;
import com.yamada.weibo.service.WeiboService;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.utils.TextUtil;
import com.yamada.weibo.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CommentLikeMapper commentLikeMapper;

    private final WeiboService weiboService;

    private final MessageService messageService;

    @Autowired
    public CommentServiceImpl(WeiboService weiboService, MessageService messageService) {
        this.weiboService = weiboService;
        this.messageService = messageService;
    }

    @Override
    public CommentVO getByCid(Integer cid) {
        Integer uid = ServletUtil.getUid();
        CommentVO commentVO = commentMapper.getByCid(cid, uid);
        if (commentVO == null) {
            throw new MyException(ResultEnum.COMMENT_NOT_EXIST);
        }
        // 设置名称、头像
        User user = userMapper.selectById(commentVO.getUid());
        commentVO.setName(user.getName());
        commentVO.setAvatar(user.getAvatar());
        // 设置text
        commentVO.setTextVOList(TextUtil.convertToTextVO(commentVO.getContent()));

        return commentVO;
    }

    @Override
    public List<CommentVO> getLevel2ByCid(Integer cid, String sort) {
        Integer uid = ServletUtil.getUid();
        List<CommentVO> commentVOList = commentMapper.getLevel2ByCid(cid, sort, uid);
        if (commentVOList.size() == 0) {
            return commentVOList;
        }
        // 设置名称、头像
        List<Integer> uidList = commentVOList.stream().map(CommentVO::getUid).collect(Collectors.toList());
        List<User> userList = userMapper.selectBatchIds(uidList);
        Map<Integer, User> map = userList.stream().collect(Collectors.toMap(User::getUid, e -> e));
        for (CommentVO commentVO : commentVOList) {
            Integer id = commentVO.getUid();
            // 设置名称、头像
            commentVO.setName(map.get(id).getName());
            commentVO.setAvatar(map.get(id).getAvatar());
            // 设置text
            commentVO.setTextVOList(TextUtil.convertToTextVO(commentVO.getContent()));
        }
        return commentVOList;
    }

    @Override
    public void add(Comment comment) {
        comment.setUid(ServletUtil.getUid());
        if (comment.getCommentCid() != null) {
            comment.setCommentType(CommentType.LEVEL2.getCode());
        } else {
            comment.setCommentType(CommentType.LEVEL1.getCode());
        }
        int result = commentMapper.insert(comment);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
        // 添加热度分数
        weiboService.increaseScore(comment.getWid(), WeiboOperationType.COMMENT);
        // 发送消息
        int loginUid = ServletUtil.getUid();
        messageService.sendAt(comment, loginUid);
        messageService.sendComment(comment, loginUid);
    }

    @Override
    public void delete(Integer cid) {
        Integer uid = ServletUtil.getUid();
        Comment comment = commentMapper.selectById(cid);
        if (comment == null) {
            throw new MyException(ResultEnum.COMMENT_NOT_EXIST);
        }
        if (!comment.getUid().equals(uid)) {
            throw new MyException(ResultEnum.NOT_AUTH);
        }
        // 删除两层评论
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("cid", cid).or().eq("comment_cid", cid);
        int result = commentMapper.delete(wrapper);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
        for (int i = 0; i < result; ++i) {
            weiboService.decreaseScore(comment.getWid(), WeiboOperationType.COMMENT);
        }
        // 删除点赞信息
        QueryWrapper<CommentLike> wrapper1 = new QueryWrapper<>();
        wrapper.eq("cid", cid);
        commentLikeMapper.delete(wrapper1);
    }
}
