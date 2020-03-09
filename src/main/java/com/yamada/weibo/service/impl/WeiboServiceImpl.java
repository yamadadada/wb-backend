package com.yamada.weibo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yamada.weibo.dto.CommentCountDTO;
import com.yamada.weibo.dto.ForwardCountDTO;
import com.yamada.weibo.dto.WeiboLikeDTO;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.enums.WeiboStatus;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.mapper.*;
import com.yamada.weibo.pojo.*;
import com.yamada.weibo.service.WeiboService;
import com.yamada.weibo.utils.FileUtil;
import com.yamada.weibo.utils.ServletUtil;
import com.yamada.weibo.vo.CommentVO;
import com.yamada.weibo.vo.WeiboLikeVO;
import com.yamada.weibo.vo.WeiboVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private CommentLikeMapper commentLikeMapper;

    @Resource
    private WeiboLikeMapper weiboLikeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private FollowMapper followMapper;

    @Value("${upload.imageHost}")
    private String imageHost;

    @Value("${upload.imagePath}")
    private String imagePath;

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
            // 添加自己的UID
            followUidList.add(uid);
            // 查询这些用户的微博
            QueryWrapper<Weibo> wrapper2 = new QueryWrapper<>();
            wrapper2.in("uid", followUidList).orderByDesc("create_time");
            PageHelper.startPage(page, size);
            List<Weibo> weiboList = weiboMapper.selectList(wrapper2);
            if (weiboList.size() > 0) {
                // 获取转发的base微博
                List<Integer> forwardWidList = weiboList.stream().filter(e -> e.getStatus().equals(WeiboStatus.FORWARD.getCode()))
                        .map(Weibo::getBaseForwardWid).collect(Collectors.toList());
                List<Weibo> list4 = weiboMapper.selectBatchIds(forwardWidList);
                Map<Integer, Weibo> map4 = list4.stream().collect(Collectors.toMap(Weibo::getWid, e -> e));
                // 获得微博作者的用户姓名和头像
                list4.addAll(weiboList);
                Map<Integer, User> userMap = getUserMap(list4);
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
                List<WeiboLikeDTO> list3 = weiboLikeMapper.weiboLikeDTOListByWidAndUid(uid, widList);
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
                    //设置转发数、评论数、点赞数、是否点赞
                    weiboVO.setForwardCount(map1.get(wid).getForwardCount());
                    weiboVO.setCommentCount(map2.get(wid).getCommentCount());
                    weiboVO.setLikeCount(map3.get(wid).getLikeCount());
                    weiboVO.setIsLike(map3.get(wid).getIsLike());
                    // 设置转发的base微博
                    if (weibo.getStatus().equals(WeiboStatus.FORWARD.getCode())) {
                        Weibo w = map4.get(weibo.getBaseForwardWid());
                        if (w != null) {
                            weiboVO.setForwardUsername(userMap.get(w.getUid()).getName());
                            weiboVO.setForwardAvatar(userMap.get(w.getUid()).getAvatar());
                            weiboVO.setForwardContent(w.getContent());
                            if (w.getImages() != null) {
                                String[] split = w.getImages().split(",");
                                weiboVO.setForwardImageList(Arrays.asList(split));
                            }
                        }
                    }
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
        // 获取总转发数
        QueryWrapper<Forward> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("forward_wid", wid);
        weiboVO.setForwardCount(forwardMapper.selectCount(wrapper1));
        // 获取总评论数
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("wid", wid);
        weiboVO.setCommentCount(commentMapper.selectCount(wrapper));
        // 获取点赞数和是否点赞
        Integer uid = ServletUtil.getUid();
        WeiboLikeDTO weiboLikeDTO = weiboLikeMapper.weiboLikeDTOByWidAndUid(uid, wid);
        weiboVO.setLikeCount(weiboLikeDTO.getLikeCount());
        weiboVO.setIsLike(weiboLikeDTO.getIsLike());
        // 如果是转发微博，设置转发的base微博
        if (weibo.getStatus().equals(WeiboStatus.FORWARD.getCode())) {
            Weibo forwardWeibo = weiboMapper.selectById(weibo.getBaseForwardWid());
            if (forwardWeibo != null) {
                User forwardUser = userMapper.selectById(forwardWeibo.getUid());
                weiboVO.setForwardUsername(forwardUser.getName());
                weiboVO.setForwardAvatar(forwardUser.getAvatar());
                weiboVO.setForwardContent(forwardWeibo.getContent());
                if (forwardWeibo.getImages() != null) {
                    String[] split = forwardWeibo.getImages().split(",");
                    weiboVO.setForwardImageList(Arrays.asList(split));
                }
            }
        }

        return weiboVO;
    }

    @Override
    public List<WeiboVO> getWeiboForward(Integer wid) {
        List<WeiboVO> weiboVOList = new ArrayList<>();

        QueryWrapper<Forward> wrapper = new QueryWrapper<>();
        wrapper.eq("forward_wid", wid);
        List<Forward> forwardList = forwardMapper.selectList(wrapper);
        List<Integer> widList = forwardList.stream().map(Forward::getWid).collect(Collectors.toList());
        if (widList.size() == 0) {
            return weiboVOList;
        }
        List<Weibo> weiboList = weiboMapper.selectBatchIds(widList);

        Map<Integer, User> userMap = getUserMap(weiboList);

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
        List<CommentVO> commentList2 = commentMapper.getLevel2ByCidList(sj.toString(), uid);
        // 设置名称、头像
        uidList.addAll(commentList2.stream().map(CommentVO::getUid).collect(Collectors.toList()));
        List<User> userList = userMapper.selectBatchIds(uidList);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getUid, e -> e));
        // 合并两层评论
        Map<Integer, CommentVO> map = commentList1.stream().collect(Collectors.toMap(CommentVO::getCid, e -> e));
        for (CommentVO comment : commentList1) {
            Integer id = comment.getUid();
            comment.setName(userMap.get(id).getName());
            comment.setAvatar(userMap.get(id).getAvatar());
        }
        for (CommentVO comment : commentList2) {
            //加入名字
            comment.setName(userMap.get(comment.getUid()).getName());

            Integer cid = comment.getCommentCid();
            map.get(cid).addComment(comment);
        }
        return commentList1;
    }

    @Override
    public Integer add(Weibo weibo) {
        Integer uid = ServletUtil.getUid();
        weibo.setUid(uid);
        weibo.setStatus(WeiboStatus.FORMAL.getCode());
        int result = weiboMapper.insert(weibo);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
        return weibo.getWid();
    }

    @Override
    public void delete(Integer wid) {
        Weibo weibo = weiboMapper.selectById(wid);
        if (weibo == null) {
            throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
        }
        Integer uid = ServletUtil.getUid();
        if (!uid.equals(weibo.getUid())) {
            throw new MyException(ResultEnum.NOT_AUTH);
        }
        int result = weiboMapper.deleteById(wid);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
        QueryWrapper<WeiboLike> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("wid", wid);
        weiboLikeMapper.delete(wrapper1);
        // 删除评论
        QueryWrapper<Comment> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("wid", wid);
        List<Comment> commentList = commentMapper.selectList(wrapper2);
        List<Integer> cidList = commentList.stream().map(Comment::getCid).collect(Collectors.toList());
        commentMapper.delete(wrapper2);
        QueryWrapper<CommentLike> wrapper3 = new QueryWrapper<>();
        if (cidList.size() > 0) {
            wrapper3.in("cid", cidList);
            commentLikeMapper.delete(wrapper3);
        }
        // 删除微博的图片
        if (weibo.getImages() != null) {
            String[] split = weibo.getImages().split(",");
            for (String image : split) {
                if (image.startsWith(imageHost)) {
                    // 删除本地图片
                    String fileName = image.substring(image.lastIndexOf("/") + 1);
                    FileUtil.delete(imagePath, fileName);
                }
            }
        }
    }

    @Override
    public void upload(Integer wid, MultipartFile file) {
        Integer uid = ServletUtil.getUid();
        Weibo weibo = weiboMapper.selectById(wid);
        if (weibo == null) {
            throw new MyException(ResultEnum.WEIBO_NOT_EXIST);
        }
        if (!weibo.getUid().equals(uid)) {
            throw new MyException(ResultEnum.NOT_AUTH);
        }
        int photoIndex = 0;
        if (weibo.getImages() != null) {
            String[] split = weibo.getImages().split(",");
            photoIndex = split.length;
        }
        String fileName = wid + "-" + photoIndex + ".png";
        if (FileUtil.upload(file, imagePath, fileName)) {
            String fullPath = imageHost + "/images/" + fileName;
            if (weibo.getImages() == null) {
                weibo.setImages(fullPath);
            } else {
                weibo.setImages(weibo.getImages() + "," + fullPath);
            }
        }
        int result = weiboMapper.updateById(weibo);
        if (result == 0) {
            throw new MyException(ResultEnum.OPERATE_ERROR);
        }
    }

    /**
     * 获取微博作者的用户信息
     * @param weiboList
     * @return
     */
    private Map<Integer, User> getUserMap(List<Weibo> weiboList) {
        List<Integer> uidList = weiboList.stream().map(Weibo::getUid).collect(Collectors.toList());
        if (uidList.size() == 0) {
            return new HashMap<>();
        }
        List<User> userList = userMapper.selectBatchIds(uidList);
        return userList.stream().collect(Collectors.toMap(User::getUid, e -> e));
    }
}
