package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wqy
 * @version 1.0
 * @date 2020/9/29 20:29
 */
public class RedisShardedPool {
    private static ShardedJedisPool pool;  // static保证JedisPool在tomcat启动时就已经加载完成
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "20"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    private static String redisIp = PropertiesUtil.getProperty("redis.ip", "127.0.0.1");
    private static int redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port", "6379"));

    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip", "127.0.0.1");
    private static int redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port", "6380"));

    // 保证单例
    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);  // 连接耗尽的时候，是否阻塞，false会抛出异常，true则会阻塞；默认值为true

        JedisShardInfo info1 = new JedisShardInfo(redisIp, redisPort, 2000);
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 2000);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>();
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);

    }

    static {
        initPool();
    }

    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    public static void returnResource(ShardedJedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static void returnBrokenResource(ShardedJedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
}
