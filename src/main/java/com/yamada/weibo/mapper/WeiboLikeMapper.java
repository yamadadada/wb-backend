package com.yamada.weibo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yamada.weibo.dto.WeiboLikeDTO;
import com.yamada.weibo.pojo.WeiboLike;
import com.yamada.weibo.vo.WeiboLikeVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WeiboLikeMapper extends BaseMapper<WeiboLike> {

    @Select("select weibo_like.*, `user`.`name`, `user`.avatar, `user`.introduction " +
            "from weibo_like left join `user` on weibo_like.uid=`user`.uid " +
            "where weibo_like.wid=#{wid} order by create_time DESC")
    List<WeiboLikeVO> getWeiboLikeList(Integer wid);

    @Select("SELECT `weibo`.wid, count(`weibo_like`.wid) like_count, " +
            "(case `weibo_like`.uid when ${uid} then 'true' else 'false' end) is_like " +
            "FROM `weibo` left join `weibo_like` on `weibo`.wid=`weibo_like`.wid " +
            "where `weibo`.wid in (${widList}) " +
            "group by `weibo`.wid")
    List<WeiboLikeDTO> weiboLikeDTOListByWidAndUid(@Param("uid") Integer uid, @Param("widList") String widList);

    @Select("SELECT `weibo`.wid, count(`weibo_like`.wid) like_count, " +
            "(case `weibo_like`.uid when ${uid} then 'true' else 'false' end) is_like " +
            "FROM `weibo` left join `weibo_like` on `weibo`.wid=`weibo_like`.wid " +
            "where `weibo`.wid=#{wid} " +
            "group by `weibo`.wid")
    WeiboLikeDTO weiboLikeDTOByWidAndUid(@Param("uid") Integer uid, @Param("wid") Integer wid);
}
