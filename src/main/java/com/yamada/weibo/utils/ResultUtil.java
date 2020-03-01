package com.yamada.weibo.utils;

import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.vo.ResultVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResultUtil {

    /**
     * 只有一个数据的时候
     * @param key
     * @param data
     * @return
     */
    public static ResponseEntity<ResultVO> success(String key, Object data) {
        Map<String, Object> map = new HashMap<>(1);
        map.put(key, data);
        ResultVO resultVO = new ResultVO(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), map);
        return ResponseEntity.status(HttpStatus.OK).body(resultVO);
    }

    public static ResponseEntity<ResultVO> success(Map<String, Object> map) {
        ResultVO resultVO = new ResultVO(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), map);
        return ResponseEntity.status(HttpStatus.OK).body(resultVO);
    }

    public static ResponseEntity<ResultVO> success(Object data) {
        ResultVO resultVO = new ResultVO(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), data);
        return ResponseEntity.status(HttpStatus.OK).body(resultVO);
    }
}
