package com.yamada.weibo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserIndexVO {

    private String indexBar;

    private List<UserVO> userVOList;

    public UserIndexVO(String indexBar) {
        this.indexBar = indexBar;
        this.userVOList = new ArrayList<>();
    }

    public void add(UserVO userVO) {
        userVOList.add(userVO);
    }
}
