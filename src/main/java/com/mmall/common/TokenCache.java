package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    private static LoadingCache<String, String> localCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(1000)
                    .maximumSize(10000)  // 缓存大于10000，guava使用LRU进行清除
                    .expireAfterAccess(12, TimeUnit.HOURS)
                    .build(new CacheLoader<String, String>() {
                        // 默认的数据加载实现。当调用get()取缓存时，key没有命中缓存，则调用load()进行加载。
                        @Override
                        public String load(String s) throws Exception {
                            return "null";
                        }
                    });

    public static void setKey(String key, String value) {
        localCache.put(key, value);
    }

    public static String getKey(String key) {
        String value = null;
        try {
            value = localCache.get(key);
            if ("null".equals(value)) {
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("localCache get() error", e);
        }
        return null;
    }
}
