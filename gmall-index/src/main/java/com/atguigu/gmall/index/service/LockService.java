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
     */
    public synchronized void testLock() {

        String number = redisTemplate.opsForValue().get("number");

        if (StringUtils.isBlank(number)) {
            redisTemplate.opsForValue().set("number", "1");
        }

        int num = Integer.parseInt(number);

        redisTemplate.opsForValue().set("number", String.valueOf(++num));

    }
}