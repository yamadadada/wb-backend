package com.yamada.weibo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yamada.weibo.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
