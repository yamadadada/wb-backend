package com.yamada.weibo.service.impl;

import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.CommentMapper;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.service.CommentService;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.vo.CommentVO;
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
            Integer id = commentVO.getCid();
            commentVO.setName(map.get(id).getName());
            commentVO.setAvatar(map.get(id).getAvatar());
        }
        return commentVOList;
    }
}
