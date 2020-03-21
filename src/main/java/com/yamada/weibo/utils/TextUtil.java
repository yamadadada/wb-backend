package com.yamada.weibo.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yamada.weibo.enums.TextType;
import com.yamada.weibo.mapper.UserMapper;
import com.yamada.weibo.pojo.User;
import com.yamada.weibo.vo.TextVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TextUtil {

    private static UserMapper userMapper;

    @Resource
    public void setUsermapper(UserMapper userMapper) {
        TextUtil.userMapper = userMapper;
    }

    public static List<TextVO> convertToTextVO(String content) {
        List<TextVO> result = new ArrayList<>();
        String regex = "(@[\\w\\u4e00-\\u9fa5]{1,16})|(#[\\w\\u4e00-\\u9fa5]+#)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        int i = 0;
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (start > i) {
                TextVO textVO = new TextVO();
                textVO.setText(content.substring(i, start));
                textVO.setTextType(TextType.NORMAL.getCode());
                result.add(textVO);
            }
            if (content.charAt(start) == '@') {
                TextVO textVO = new TextVO();
                textVO.setText(content.substring(start, end));
                int uid = getUidByName(content.substring(start + 1, end));
                if (uid == -1) {
                    textVO.setTextType(TextType.NORMAL.getCode());
                    result.add(textVO);
                } else {
                    textVO.setTextType(TextType.AT.getCode());
                    textVO.setUid(uid);
                    textVO.setName(content.substring(start + 1, end));
                    result.add(textVO);
                }
            } else {
                TextVO textVO = new TextVO();
                textVO.setText(content.substring(start, end));
                textVO.setTextType(TextType.TOPIC.getCode());
                textVO.setTopic(content.substring(start + 1, end - 1));
                result.add(textVO);
            }
            i = end;
        }
        if (i < content.length()) {
            TextVO textVO = new TextVO();
            textVO.setText(content.substring(i));
            textVO.setTextType(TextType.NORMAL.getCode());
            result.add(textVO);
        }
        return result;
    }

    public static List<String> parseTopic(String content) {
        List<String> result = new ArrayList<>();
        String regex = "#[\\w\\u4e00-\\u9fa5]+#";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            result.add(content.substring(m.start() + 1, m.end() - 1));
        }
        return result;
    }

    /**
     * 通过名字查找uid，不存在返回-1
     * @param name
     * @return
     */
    private static int getUidByName(String name) {
        if (name.length() == 0) {
            return -1;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return -1;
        }
        return user.getUid();
    }
}
