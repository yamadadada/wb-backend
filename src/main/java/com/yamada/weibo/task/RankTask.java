package com.yamada.weibo.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@EnableScheduling
@Slf4j
public class RankTask {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RankTask(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 * * * * *")
    public void hotSearch() {
        log.info("【定时任务】热门搜索");
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores("hot-list", 0, -1);
        if (tuples != null) {
            int i = 0;
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (i >= 50 && tuple.getScore() != null && tuple.getScore() <= 1) {
                    redisTemplate.opsForZSet().remove("hot-list", tuple.getValue());
                } else if (tuple.getScore() != null && tuple.getScore() > 1) {
                    double increment = tuple.getScore() / 2.0;
                    redisTemplate.opsForZSet().incrementScore("hot-list", tuple.getValue(), -increment);
                }
                ++i;
            }
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void hotWeibo() {
        log.info("【定时任务】热门微博");
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores("hot-weibo-zset", 0, -1);
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (tuple.getScore() != null && tuple.getScore() <= 1) {
                    redisTemplate.opsForZSet().remove("hot-weibo-zset", tuple.getValue());
                } else if (tuple.getScore() != null && tuple.getScore() > 1) {
                    double increment = tuple.getScore() / 2.0;
                    redisTemplate.opsForZSet().incrementScore("hot-weibo-zset", tuple.getValue(), -increment);
                }
            }
        }
    }
}
