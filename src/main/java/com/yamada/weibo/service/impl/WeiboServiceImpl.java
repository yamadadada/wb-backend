package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yamada.weibo.dto.CommentCountDTO;
import com.yamada.weibo.dto.ForwardCountDTO;
import com.yamada.weibo.dto.WeiboLikeDTO;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.*;
import com.yamada.weibo.pojo.*;
import com.yamada.weibo.service.WeiboService;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.vo.CommentVO;
import com.yamada.weibo.vo.WeiboLikeVO;
import com.yamada.weibo.vo.WeiboVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeiboServiceImpl implements WeiboService {

    @Resource
    private WeiboMapper weiboMapper;

    @Resource
    private ForwardMapper forwardMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private WeiboLikeMapper weiboLikeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FollowMapper followMapper;

    @Override
    public List<WeiboVO> getFollowWeibo(Integer page, Integer size) {
        List<WeiboVO> weiboVOList = new ArrayList<>();
        // 获得已关注的用户列表
        Integer uid = ServletUtil.getUid();
        QueryWrapper<Follow> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("uid", uid);
        List<Follow> followList = followMapper.selectList(wrapper1);
        if (followList.size() > 0) {
            List<Integer> followUidList = followList.stream().map(Follow::getFollowUid).collect(Collectors.toList());
            // 查询这些用户的微博
            QueryWrapper<Weibo> wrapper2 = new QueryWrapper<>();
            wrapper2.in("uid", followUidList).orderByDesc("create_time");
            PageHelper.startPage(page, size);
            List<Weibo> weiboList = weiboMapper.selectList(wrapper2);
            if (weiboList.size() > 0) {
                // 获得微博作者的用户姓名和头像
                Map<Integer, User> userMap = getUserMap(weiboList);
                // 获取转发数
                StringJoiner sj = new StringJoiner(",");
                for (Weibo weibo : weiboList) {
                    sj.add(String.valueOf(weibo.getWid()));
                }
                String widList = sj.toString();
                List<ForwardCountDTO> list1 = forwardMapper.forwardCountByWid(widList);
                Map<Integer, ForwardCountDTO> map1 = list1.stream().collect(Collectors.toMap(ForwardCountDTO::getWid, e -> e));
                // 获取评论数
                List<CommentCountDTO> list2 = commentMapper.commentCountByWid(widList);
                Map<Integer, CommentCountDTO> map2 = list2.stream().collect(Collectors.toMap(CommentCountDTO::getWid, e -> e));
                // 获取点赞数和是否点赞
                List<WeiboLikeDTO> list3 = weiboLikeMapper.getWeiboLikeDTOByWidAndUid(uid, widList);
                Map<Integer, WeiboLikeDTO> map3 = list3.stream().collect(Collectors.toMap(WeiboLikeDTO::getWid, e -> e));
                // 转换为VO
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (Weibo weibo : weiboList) {
                    Integer wid = weibo.getWid();
                    WeiboVO weiboVO = new WeiboVO();
                    BeanUtils.copyProperties(weibo, weiboVO);
                    weiboVO.setFullTime(sdf.format(weibo.getCreateTime()));
                    // 解析图片
                    if (weibo.getImages() != null) {
                        String[] split = weibo.getImages().split(",");
                        weiboVO.setImageList(Arrays.asList(split));
                    }
                    // 加入微博作者的用户姓名和头像
                    User user = userMap.get(weibo.getUid());
                    weiboVO.setName(user.getName());
                    weiboVO.setAvatar(user.getAvatar());
                    //设置转发数
                    weiboVO.setForwardCount(map1.get(wid).getForwardCount());
                    weiboVO.setCommentCount(map2.get(wid).getCommentCount());
                    weiboVO.setLikeCount(map3.get(wid).getLikeCount());
                    weiboVO.setIsLike(map3.get(wid).getIsLike());

                    weiboVOList.add(weiboVO);
                }
            }
        }
        return weiboVOList;
    }

    @Override
    public WeiboVO getWeiboDetail(Integer wid) {
        Weibo weibo = weiboMapper.selectById(wid);
        if (weibo == null) {
            throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
        }
        WeiboVO weiboVO = new WeiboVO();
        BeanUtils.copyProperties(weibo, weiboVO);
        // 设置图片
        if (weibo.getImages() != null) {
            String[] split = weibo.getImages().split(",");
            weiboVO.setImageList(Arrays.asList(split));
        }
        // 设置名称、头像
        User user = userMapper.selectById(weibo.getUid());
        weiboVO.setName(user.getName());
        weiboVO.setAvatar(user.getAvatar());
        // 设置评论
        PageHelper.startPage(1, 10);
        weiboVO.setCommentVOList(getCommentByWid(wid, "like_count"));
        // 获取总评论数
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("wid", wid);
        weiboVO.setCommentCount(commentMapper.selectCount(wrapper));
        return weiboVO;
    }

    @Override
    public List<WeiboVO> getWeiboForward(Integer wid) {
        QueryWrapper<Forward> wrapper = new QueryWrapper<>();
        wrapper.eq("forward_wid", wid);
        List<Forward> forwardList = forwardMapper.selectList(wrapper);
        List<Integer> widList = forwardList.stream().map(Forward::getWid).collect(Collectors.toList());
        List<Weibo> weiboList = weiboMapper.selectBatchIds(widList);

        Map<Integer, User> userMap = getUserMap(weiboList);

        List<WeiboVO> weiboVOList = new ArrayList<>();
        for (Weibo weibo : weiboList) {
            WeiboVO weiboVO = new WeiboVO();
            BeanUtils.copyProperties(weibo, weiboVO);
            // 加入微博作者信息
            weiboVO.setName(userMap.get(weibo.getUid()).getName());
            weiboVO.setAvatar(userMap.get(weibo.getUid()).getAvatar());

            weiboVOList.add(weiboVO);
        }
        return weiboVOList;
    }

    @Override
    public Map<String, Object> getWeiboComment(Integer wid, String sort) {
        Map<String, Object> map = new HashMap<>();
        List<CommentVO> commentList = getCommentByWid(wid, sort);
        map.put("commentVOList", commentList);
        // 获取总评论数
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("wid", wid);
        map.put("commentCount", commentMapper.selectCount(wrapper));
        return map;
    }

    @Override
    public List<WeiboLikeVO> getWeiboLike(Integer wid) {
        return weiboLikeMapper.getWeiboLikeList(wid);
    }

    /**
     * 查找指定微博的评论，处理评论之间的关系
     * @param wid
     * @return
     */
    private List<CommentVO> getCommentByWid(Integer wid, String sort) {
        Integer uid = ServletUtil.getUid();
        // 获取该微博的第一层评论
        List<CommentVO> commentList1 = commentMapper.getLevel1ByWid(wid, sort, uid);
        if (commentList1.size() == 0) {
            return new ArrayList<>();
        }
        List<Integer> uidList = commentList1.stream().map(CommentVO::getUid).collect(Collectors.toList());
        // 获取该微博的第二层评论
        StringJoiner sj = new StringJoiner(",");
        for (CommentVO commentVO : commentList1) {
            sj.add(String.valueOf(commentVO.getCid()));
        }
        // 设置名称、头像
        List<CommentVO> commentList2 = commentMapper.getLevel2ByCidList(sj.toString(), uid);
        uidList.addAll(commentList2.stream().map(CommentVO::getUid).collect(Collectors.toList()));
        List<User> userList = userMapper.selectBatchIds(uidList);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getUid, e -> e));
        // 合并两层评论
        Map<Integer, CommentVO> map = commentList1.stream().collect(Collectors.toMap(CommentVO::getCid, e -> e));
        for (CommentVO comment : commentList1) {
            Integer cid = comment.getCid();
            comment.setName(userMap.get(cid).getName());
            comment.setAvatar(userMap.get(cid).getAvatar());
        }
        for (CommentVO comment : commentList2) {
            Integer cid = comment.getCommentCid();
            map.get(cid).addComment(comment);
        }
        return commentList1;
    }

    /**
     * 获取微博作者的用户信息
     * @param weiboList
     * @return
     */
    private Map<Integer, User> getUserMap(List<Weibo> weiboList) {
        List<Integer> uidList = weiboList.stream().map(Weibo::getUid).collect(Collectors.toList());
        List<User> userList = userMapper.selectBatchIds(uidList);
        return userList.stream().collect(Collectors.toMap(User::getUid, e -> e));
    }
}
