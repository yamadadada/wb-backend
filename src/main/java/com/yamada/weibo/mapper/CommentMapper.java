package com.yamada.weibo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yamada.weibo.dto.CommentCountDTO;
import com.yamada.weibo.pojo.Comment;
import com.yamada.weibo.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    @Select("select `comment`.*, count(`comment_like`.uid) like_count, " +
            "(case `comment_like`.uid when #{uid} then 'true' else 'false' end) is_like " +
            "from `comment` left join `comment_like` on `comment`.cid=`comment_like`.cid " +
            "where `comment`.wid=#{wid} and `comment`.comment_type=0 " +
            "GROUP BY `comment`.cid ORDER BY ${sort} DESC")
    List<CommentVO> getLevel1ByWid(@Param("wid") Integer wid, @Param("sort") String sort, @Param("uid") Integer uid);

    @Select("select `comment`.*, count(`comment_like`.uid) like_count,  " +
            "(case `comment_like`.uid when #{uid} then 'true' else 'false' end) is_like " +
            "from `comment` left join `comment_like` on `comment`.cid=`comment_like`.cid " +
            "where `comment`.comment_cid in (${cidList}) " +
            "GROUP BY `comment`.cid ORDER BY like_count DESC")
    List<CommentVO> getLevel2ByCidList(@Param("cidList") String cidList, @Param("uid") Integer uid);

    @Select("select `comment`.*, count(`comment_like`.uid) like_count, " +
            "(case `comment_like`.uid when #{uid} then 'true' else 'false' end) is_like " +
            "from `comment` left join `comment_like` on `comment`.cid=`comment_like`.cid " +
            "where `comment`.cid=#{cid} " +
            "GROUP BY `comment`.cid")
    CommentVO getByCid(@Param("cid") Integer cid, @Param("uid") Integer uid);

    @Select("select `comment`.*, count(`comment_like`.uid) like_count, " +
            "(case `comment_like`.uid when #{uid} then 'true' else 'false' end) is_like " +
            "from `comment` left join `comment_like` on `comment`.cid=`comment_like`.cid " +
            "where `comment`.comment_cid=#{cid} " +
            "GROUP BY `comment`.cid ORDER BY ${sort} DESC")
    List<CommentVO> getLevel2ByCid(@Param("cid") Integer cid, @Param("sort") String sort, @Param("uid") Integer uid);

    @Select("SELECT `weibo`.wid, count(`comment`.wid) comment_count " +
            "FROM `weibo` " +
            "left join `comment` on `weibo`.wid=`comment`.wid " +
            "where `weibo`.wid in (${widList}) " +
            "group by `weibo`.wid")
    List<CommentCountDTO> commentCountByWid(@Param("widList") String widList);
}
