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

    @Resource
    private FavoriteMapper favoriteMapper;

    @Override
    public List<WeiboVO> getFollowWeibo(Integer page, Integer size) {
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
            return toWeiboVOList(weiboList);
        }
        return new ArrayList<>();
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
        // 获得是否点赞
        QueryWrapper<Favorite> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("uid", uid).eq("wid", weiboVO.getWid());
        Favorite favorite = favoriteMapper.selectOne(wrapper2);
        if (favorite == null) {
            weiboVO.setIsFavorite(false);
        } else {
            weiboVO.setIsFavorite(true);
        }
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

        List<Integer> uidList = weiboList.stream().map(Weibo::getUid).collect(Collectors.toList());
        Map<Integer, User> userMap = userMapper.selectBatchIds(uidList).stream().collect(Collectors.toMap(User::getUid, e -> e));

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
                if (image.startsWith(FileUtil.imageHost)) {
                    // 删除本地图片
                    String fileName = image.substring(image.lastIndexOf("/") + 1);
                    FileUtil.delete(FileUtil.imagePath, fileName);
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
        if (FileUtil.upload(file, FileUtil.imagePath, fileName)) {
            String fullPath = FileUtil.imageHost + "/images/" + fileName;
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

    @Override
    public List<WeiboVO> getByUid(Integer uid) {
        QueryWrapper<Weibo> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).orderByDesc("create_time");
        List<Weibo> weiboList = weiboMapper.selectList(wrapper);
        return toWeiboVOList(weiboList);
    }

    @Override
    public List<WeiboVO> myLike() {
        Integer loginUid = ServletUtil.getUid();
        QueryWrapper<WeiboLike> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", loginUid).orderByDesc("create_time");
        List<WeiboLike> likeList = weiboLikeMapper.selectList(wrapper);
        if (likeList.size() == 0) {
            return new ArrayList<>();
        }
        List<Integer> widList = likeList.stream().map(WeiboLike::getWid).collect(Collectors.toList());
        return toWeiboVOList(weiboMapper.selectBatchIds(widList));
    }

    @Override
    public List<WeiboVO> myFavorite() {
        Integer loginUid = ServletUtil.getUid();
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", loginUid).orderByDesc("create_time");
        List<Favorite> favoriteList = favoriteMapper.selectList(wrapper);
        if (favoriteList.size() == 0) {
            return new ArrayList<>();
        }
        List<Integer> widList = favoriteList.stream().map(Favorite::getWid).collect(Collectors.toList());
        return toWeiboVOList(weiboMapper.selectBatchIds(widList));
    }

    /**
     * 解析图片地址
     * @param weibo
     * @return
     */
    private List<String> parseImage(Weibo weibo) {
        if (weibo.getImages() != null) {
            String[] split = weibo.getImages().split(",");
            for (int i = 0; i < split.length; i++) {
                if (split[i].startsWith("/images/")) {
                    split[i] = FileUtil.imageHost + split[i];
                }
            }
            return Arrays.asList(split);
        }
        return null;
    }

    private List<WeiboVO> toWeiboVOList(List<Weibo> weiboList) {
        List<WeiboVO> weiboVOList = new ArrayList<>();
        if (weiboList.size() == 0) {
            return weiboVOList;
        }
        // 获取转发的base微博
        Map<Integer, Weibo> forwardMap = new HashMap<>();
        List<Integer> forwardWidList = weiboList.stream().filter(e -> e.getStatus().equals(WeiboStatus.FORWARD.getCode()))
                .map(Weibo::getBaseForwardWid).collect(Collectors.toList());
        if (forwardWidList.size() > 0) {
            forwardMap = weiboMapper.selectBatchIds(forwardWidList).stream().collect(Collectors.toMap(Weibo::getWid, e -> e));
        }
        // 获得微博（包括转发的base）作者的用户姓名和头像
        List<Weibo> list = new ArrayList<>(forwardMap.values());
        list.addAll(weiboList);
        List<Integer> uidList = list.stream().map(Weibo::getUid).collect(Collectors.toList());
        Map<Integer, User> userMap = userMapper.selectBatchIds(uidList).stream().collect(Collectors.toMap(User::getUid, e -> e));
        // 获取转发数
        StringJoiner sj = new StringJoiner(",");
        for (Weibo weibo : weiboList) {
            sj.add(String.valueOf(weibo.getWid()));
        }
        String widList = sj.toString();
        List<ForwardCountDTO> list1 = forwardMapper.forwardCountByWid(widList);
        Map<Integer, ForwardCountDTO> forwardCountMap = list1.stream().collect(Collectors.toMap(ForwardCountDTO::getWid, e -> e));
        // 获取评论数
        List<CommentCountDTO> list2 = commentMapper.commentCountByWid(widList);
        Map<Integer, CommentCountDTO> commentCountMap = list2.stream().collect(Collectors.toMap(CommentCountDTO::getWid, e -> e));
        // 获取点赞数和是否点赞
        int uid = ServletUtil.getUid();
        List<WeiboLikeDTO> list3 = weiboLikeMapper.weiboLikeDTOListByWidAndUid(uid, widList);
        Map<Integer, WeiboLikeDTO> likeMap = list3.stream().collect(Collectors.toMap(WeiboLikeDTO::getWid, e -> e));
        // 获得是否收藏
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).in("wid", weiboList.stream().map(Weibo::getWid).collect(Collectors.toList()));
        Set<Integer> isFavoriteSet = favoriteMapper.selectList(wrapper).stream().map(Favorite::getWid).collect(Collectors.toSet());
        // 转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Weibo weibo : weiboList) {
            int wid = weibo.getWid();
            WeiboVO weiboVO = new WeiboVO();
            BeanUtils.copyProperties(weibo, weiboVO);
            weiboVO.setFullTime(sdf.format(weibo.getCreateTime()));
            // 解析图片
            weiboVO.setImageList(parseImage(weibo));
            // 加入微博作者的用户姓名和头像
            User user = userMap.get(weibo.getUid());
            weiboVO.setName(user.getName());
            weiboVO.setAvatar(user.getAvatar());
            // 设置转发的base微博
            if (weibo.getStatus().equals(WeiboStatus.FORWARD.getCode())) {
                Weibo baseWeibo = forwardMap.get(weibo.getBaseForwardWid());
                if (baseWeibo != null) {
                    weiboVO.setForwardUsername(userMap.get(baseWeibo.getUid()).getName());
                    weiboVO.setForwardAvatar(userMap.get(baseWeibo.getUid()).getAvatar());
                    weiboVO.setForwardContent(baseWeibo.getContent());
                    weiboVO.setForwardImageList(parseImage(baseWeibo));
                }
            }
            // 加入转发数
            weiboVO.setForwardCount(forwardCountMap.get(wid).getForwardCount());
            // 加入评论数
            weiboVO.setCommentCount(commentCountMap.get(wid).getCommentCount());
            // 加入点赞数
            weiboVO.setLikeCount(likeMap.get(wid).getLikeCount());
            // 加入是否点赞
            weiboVO.setIsLike(likeMap.get(wid).getIsLike());
            // 加入是否收藏
            if (isFavoriteSet.contains(wid)) {
                weiboVO.setIsFavorite(true);
            } else {
                weiboVO.setIsFavorite(false);
            }

            weiboVOList.add(weiboVO);
        }
        return weiboVOList;
    }
}
