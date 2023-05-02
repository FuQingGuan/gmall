package com.atguigu.gmall.index.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/3 05:01
 * @Email: moumouguan@gmail.com
 */
@Service
public class LockService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 测试. 每次 将 number 值设置为 0 通过 ab 压测工具 ab -n 5000 -c 100 192.168.0.111:8888/index/test/lock 测试高并发下是否出现并发问题(number 未到 5000 即出现并发问题)
     *  ab压测: ab  -n（一次发送的请求数）  -c（请求的并发数） 访问路径
     *      1. 将 number 值设置为 0, 通过浏览器访问 每访问一次 number + 1
     *      2. 通过 ab 压测工具 ab -n 5000 -c 100 192.168.0.111:8888/index/test/lock 测试高并发下是否出现并发问题(number 未到 5000 即出现并发问题)
     *          最终 number 值为 201 出现并发性问题
     *      3. 添加 synchronized jvm 锁 将 number 设置为 0 压测 5000
     *          最终 number 值为 5000, 要注意的是 这只是单机工程
     *      4. copy 2 份实例 将 number 设置为 0 压测 5000
     *          最终 number 值为 1928, 出现并发性问题. 理论值在 5000 / 3 至 5000(极限情况下 3 台服务同时放入一个线程 同时到达 都将 num 转换为某一个数字 ++.)
     *
     * 基于 redis 实现分布式锁。借助于 setnx 指令 当 key 不存在即设置成功返回 1 当 key 存在即设置失败返回 0(加锁 解锁 重试)
     *      分布式锁特征: 独占排他互斥使用
     */
    public void testLock() {
        /**
         * 加锁 setIfAbsent 类似与 setNx 当 key 不存在即设置成功 否 则 失败
         *      分布式锁本质就是对 key 的争抢, 谁先设置成功谁就先获取锁
         */
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", "lock");

        if (!flag) {
            // 加锁失败, 进行递归调用进行重试
            try {
                // 睡眠一段时间(如果不设置睡眠不停的重试也可能会导致栈内存溢出) 模拟让抢到锁的线程执行业务逻辑 减少竞争
                Thread.sleep(30);
                // 设置锁(加锁)失败重新调用该方法进行重试
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            String number = redisTemplate.opsForValue().get("number");

            if (StringUtils.isBlank(number)) {
                redisTemplate.opsForValue().set("number", "1");
            }

            int num = Integer.parseInt(number);

            redisTemplate.opsForValue().set("number", String.valueOf(++num));

            // 释放锁
            redisTemplate.delete("lock");
        }
    }
}