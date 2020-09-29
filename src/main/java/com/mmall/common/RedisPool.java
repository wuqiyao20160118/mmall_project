package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author wqy
 * @version 1.0
 * @date 2020/9/29 0:53
 */
public class RedisPool {

    private static JedisPool pool;  // static保证JedisPool在tomcat启动时就已经加载完成
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "20"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    private static String redisIp = PropertiesUtil.getProperty("redis.ip", "127.0.0.1");
    private static int redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port", "6379"));

    // 保证单例
    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);  // 连接耗尽的时候，是否阻塞，false会抛出异常，true则会阻塞；默认值为true

        pool = new JedisPool(config, redisIp, redisPort, 2000);
    }

    static {
        initPool();
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }

    public static void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static void returnBrokenResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
}
