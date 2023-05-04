package com.atguigu.gmall.index.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @Description: 分布式锁
 * @Author: Guan FuQing
 * @Date: 2023/5/4 20:07
 * @Email: moumouguan@gmail.com
 */
@Component
public class DistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 可重入 加锁  hash Map<lockName, Map<uuid, 重入次数>>
     *     实现思路
     *          判断锁是否存在(exists), 如果不存在(0) 则直接获取锁(hset)
     *          判断是否自己的锁(hexists), 如果是(1)则重入(hincrby)
     *          否则获取锁失败, 返回 0
     *
     *      if redis.call('exists', 'lock') == 0 or redis.call ('hexists', 'lock', 'uuid') == 1
     *      then
     * 	        redis.call('hexists', 'lock', 'uuid', 1)
     * 	        redis.call('expire', 'lock', 30)
     * 	        return 1
     *      else
     * 	        return 0
     *      end
     *
     * @param lockName 锁名称
     * @param uuid 锁的唯一标识
     * @param expire 过期时间
     * @return
     */
    public Boolean tryLock(String lockName, String uuid, Integer expire) {
        String script = "if redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1" +
                "then" +
                "   redis.call('hincrby', KEYS[1], ARGV[1], 1)" +
                "   redis.call('expire', KEYS[1], ARGV[2])" +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end";

        /**
         * 使用 RedisTemplate 的 execute 方法执行 Redis Lua 脚本。
         *      传入的脚本是一个字符串变量 script，表示要执行的 Lua 脚本，其中 KEYS[1] 表示 Redis 中存储锁的键名 lockName，ARGV[1] 表示传入的锁的值（即 uuid）， ARGV[1] 表示传入的锁的值2（即 过期时间）。
         *      使用 Arrays.asList 方法构造了一个包含 "lockName" key，作为参数传入脚本。arg 是 uuid 与 expire
         *
         *      public <T> T execute(RedisScript<T> script, List<K> keys, Object... args)
         *                                  lua 脚本       ,   key 集合   ,    arg 列表
         */
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString());

        if (!flag) {
            // 获取锁失败, 重试
            try {
                Thread.sleep(40);
                tryLock(lockName, uuid, expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 获取到锁, 返回true
        return true;
    }

}
