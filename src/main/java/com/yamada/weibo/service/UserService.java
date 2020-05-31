package com.yamada.weibo.service;

import com.yamada.weibo.pojo.User;
import com.yamada.weibo.vo.UserIndexVO;
import com.yamada.weibo.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserVO getUserInfo(Integer uid);

    void update(Integer uid, User user);

    List<UserVO> getSimpleUserVO(String column);

    void updateAvatar(Integer uid, MultipartFile file);

    List<UserVO> searchByName(String name);

    List<UserIndexVO> getUserIndex();

    UserVO getByName(String name);

    void ban(Integer uid);

    List<String> searchSchool(String school);
}
