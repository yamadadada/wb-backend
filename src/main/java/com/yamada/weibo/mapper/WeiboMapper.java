package com.yamada.weibo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yamada.weibo.pojo.Weibo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WeiboMapper extends BaseMapper<Weibo> {

    @Select("SELECT * FROM `weibo` where weibo.uid in (select follow.follow_uid from follow where follow.uid=#{uid}) " +
            "or weibo.uid=#{uid} order by create_time desc")
    List<Weibo> getFollowWeibo(@Param("uid") Integer uid);
}
