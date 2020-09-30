package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedisShardedPool;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @author wqy
 * @version 1.0
 * @date 2020/9/30 10:31
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    @PreDestroy  // Tomcat退出时使用shutdown方法可以触发@PreDestory注解的方法，这里即释放锁持有的分布式锁
    public void delLock() {
        RedisShardedUtil.del(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
    }

    // @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    //@Scheduled(cron = "0 */1 * * * ?")  // 每一分钟
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));

        Long setnxResult = RedisShardedUtil.setnx(Const.RedisLock.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeout));

        if (setnxResult != null && setnxResult.intValue() == 1) {
            // 如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("没有获得分布式锁: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        }

        log.info("关闭订单定时任务结束");
    }

    @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));

        Long setnxResult = RedisShardedUtil.setnx(Const.RedisLock.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + lockTimeout));

        if (setnxResult != null && setnxResult.intValue() == 1) {
            // 如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        } else {
            // 未获取到锁，通过时间戳，判断是否可以重置并获取到锁（锁超时的情况）
            String lockValueStr = RedisShardedUtil.get(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                String getSetResult = RedisShardedUtil.getSet(Const.RedisLock.CLOSE_ORDER_TASK_LOCK,
                        String.valueOf(System.currentTimeMillis() + lockTimeout));

                // 判断是否有别的进程修改了分布式锁（即是否可以获取分布式锁）
                if (getSetResult == null || StringUtils.equals(lockValueStr, getSetResult)) {
                    // 可以获取到分布式锁
                    closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取到分布式锁: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                log.info("没有获取到分布式锁: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
            }
        }

        log.info("关闭订单定时任务结束");
    }

    // 使用Redisson进行分布式锁的控制
    public void closeOrderTaskV4() {
        log.info("关闭订单定时任务启动");
        RLock lock = redissonManager.getRedisson().getLock(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);

        boolean getLock = false;
        try {
            // 这里需要注意waitTime的设置，不宜太长，一般情况下设置为0即可
            if (getLock = lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                log.info("Redisson获取分布式锁: {}, ThreadName: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK
                        , Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
                iOrderService.closeOrder(hour);
            } else {
                log.info("没有获取到分布式锁: {}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
            }
        } catch (InterruptedException e) {
            log.error("Redisson分布式锁获取异常", e);
        } finally {
            if (!getLock) {
                return;
            }
            // 释放分布式锁
            lock.unlock();
            log.info("Redisson分布式锁已被释放");
        }

        log.info("关闭订单定时任务结束");
    }

    private void closeOrder(String lockName) {
        // 有效期为5秒，防止死锁
        RedisShardedUtil.expire(lockName, 5);
        log.info("获取{}, ThreadName: {}", lockName, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);

        // 释放锁
        RedisShardedUtil.del(lockName);
        log.info("释放{}, ThreadName: {}", lockName, Thread.currentThread().getName());
    }

}
