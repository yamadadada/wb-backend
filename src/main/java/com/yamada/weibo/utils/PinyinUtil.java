package com.yamada.weibo.utils;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PinyinUtil {

    /**
     * 转换为不带音调的拼音字符串
     * @param str
     * @return
     */
    public static String toPinyin(String str) {
        String result = null;
        try {
            result = PinyinHelper.convertToPinyinString(str, "", PinyinFormat.WITHOUT_TONE).toUpperCase();
        } catch (PinyinException e) {
            log.error("【转换拼音失败】" + str);
        }
        return result;
    }

    public static int sortByPinyin(String chinese1, String chinese2) {
        String s1 = toPinyin(chinese1);
        String s2 = toPinyin(chinese2);
        int i = 0;
        while (i < s1.length() && i < s2.length()) {
            if (s1.charAt(i) < s2.charAt(i)) {
                return -1;
            }
            if (s1.charAt(i) > s2.charAt(i)) {
                return 1;
            }
            i++;
        }
        if (i == s1.length() && i == s2.length()) {
            return 0;
        }
        if (i == s1.length()) {
            return -1;
        }
        return 1;
    }

    public static String getFirstLetter(String str) {
        if (str.length() == 1) {
            return "";
        }
        try {
            return PinyinHelper.getShortPinyin(str.substring(0, 1)).toUpperCase();
        } catch (PinyinException e) {
            log.error("【获取拼音首字母失败】" + str);
        }
        return null;
    }
}
