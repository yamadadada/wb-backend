package com.yamada.weibo.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yamada.weibo.enums.ResultEnum;
import com.yamada.weibo.exception.MyException;
import com.yamada.weibo.pojo.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtil {

    @Value("${wx.secret}")
    private String TOKEN_SECRET;

    @Value("${wx.expire_time}")
    private Long expire_time;

    public String create(User user) {
        // 设置header
        HashMap<String, Object> header = new HashMap<>(2);
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        // 过期时间
        Date expireTime = new Date(System.currentTimeMillis() + expire_time);
        // 设置算法和密钥
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        return JWT.create().withHeader(header).withClaim("uid", user.getUid())
                .withClaim("name", user.getName()).withClaim("avatar", user.getAvatar())
                .withExpiresAt(expireTime).sign(algorithm);
    }

    /**
     * 验证token，返回uid
     * @param token
     * @return
     */
    public Integer verify(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("uid").asInt();
        } catch (JWTVerificationException e) {
            throw new MyException(ResultEnum.TOKEN_ERROR);
        }
    }
}
