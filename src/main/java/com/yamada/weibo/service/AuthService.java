package com.yamada.weibo.service;

import com.yamada.weibo.form.UserInfoForm;

import java.util.Map;

public interface AuthService {

    Map<String, Object> login(String code);

    Map<String, Object> getUserInfo(UserInfoForm userInfoForm);
}
