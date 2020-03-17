package com.yamada.weibo.utils;

import org.junit.jupiter.api.Test;

public class PinyinUtilTest {

    @Test
    public void test() {
        String s = "_";
        String pinyin = PinyinUtil.getFirstLetter(s);
        System.out.println(pinyin);
    }
}
