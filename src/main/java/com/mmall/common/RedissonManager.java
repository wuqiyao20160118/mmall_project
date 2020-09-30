package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author wqy
 * @version 1.0
 * @date 2020/9/30 16:38
 */
@Component
@Slf4j
public class RedissonManager {

    private Config config = new Config();

    private Redisson redisson = null;

    public Redisson getRedisson() {
        return redisson;
    }

    private static String redisIp = PropertiesUtil.getProperty("redis.ip", "127.0.0.1");
    private static int redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port", "6379"));
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip", "127.0.0.1");
    private static int redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port", "6380"));

    @PostConstruct
    private void init() {
        try {
            config.useSingleServer().setAddress(redisIp + ":" + redisPort);
            redisson = (Redisson) Redisson.create(config);

            log.info("初始化Redisson结束");
        } catch (Exception e) {
            log.error("初始化Redisson发生错误", e);
        }
    }
}
