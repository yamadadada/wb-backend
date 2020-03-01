package com.yamada.weibo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yamada.weibo.dto.ForwardCountDTO;
import com.yamada.weibo.pojo.Forward;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ForwardMapper extends BaseMapper<Forward> {

    @Select("SELECT `weibo`.wid, count(forward_wid) forward_count " +
            "FROM `weibo` " +
            "left join `forward` on `weibo`.wid=`forward`.forward_wid " +
            "where `weibo`.wid in (${widList}) " +
            "group by `weibo`.wid")
    List<ForwardCountDTO> forwardCountByWid(@Param("widList") String widList);
}
