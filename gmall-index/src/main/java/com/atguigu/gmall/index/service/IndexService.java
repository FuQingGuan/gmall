package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/2 22:28
 * @Email: moumouguan@gmail.com
 */
@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 模块名称作为第一位 找到团队的缓存
     * 模型名称作为第二位 找到工程的缓存
     * 真正的key作为第三位 找到真正的值
     */
    private static final String KEY_PREFIX = "INDEX:CATES:";
    // 分布式锁应该也需要存在唯一标识只需要锁住当前访问热点数据即可
    private static final String LOCK_PREFIX = "INDEX:CATES:LOCK:";

    public List<CategoryEntity> queryLvl1Categories() {
        // 通过已有接口直接调用 传参 0 即可查询全部一级分类
        ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryCategoriesByPid(0L);

        return categoryResponseVo.getData();
    }

    // 一般 自定义前缀 + 请求参数作为 key, 返回结果集作为 value 放入缓存
    public List<CategoryEntity> queryLvl23CategoriesByPid(Long pid) {
        // 1. 先查询缓存, 如果缓存命中则返回
        String json = redisTemplate.opsForValue().get(
                KEY_PREFIX + pid
        );
        // 不为空则
        if (StringUtils.isNotBlank(json)) {
            // 将 Json 字符串数据转换成集合对象
            return JSON.parseArray(json, CategoryEntity.class); // json 字符串, 转换类型
        }

        /**
         * 缓存击穿: 当一个热点数据过期, 大量请求直达数据库导致 数据库宕机
         *      解决方案: 添加分布式锁
         *
         *      注意
         *          在哪里加锁
         *              1. 查询 DB 前加锁, 加在此后面则将无意义，大量请求已到达 DB。
         *              2. 应该在查询缓存后加锁，查询缓存前加锁即使 Redis 中有数据也不能并发
         *          加锁后再次查询缓存
         *              1. 加锁后应该再次查询缓存，因为 当前请求获取锁的过程中, 可能有其他请求已经把数据放入缓存, 此时, 可以再次查询缓存, 如果命中则直接返回
         *
         *      当 DB 某一个热点数据过期后，假设 1 - 10 秒是 第一个获取分布式锁完成业务并把数据放入缓存完成的线程所需要的所有过程以及时间
         *      在此过程中的每个请求都会获取一次分布式锁，因为无法取消。当 10 秒后所有的请求直接经过 缓存 不会在经过 分布式锁，直到下一个热点数据过期
         */
        RLock fairLock = redissonClient.getFairLock(LOCK_PREFIX + pid);
        fairLock.lock();

        try {
            // 当前请求获取锁的过程中, 可能有其他请求已经把数据放入缓存, 此时, 可以再次查询缓存, 如果命中则直接返回
            String json2 = redisTemplate.opsForValue().get(
                    // key, value
                    KEY_PREFIX + pid
            );

            // 不为空则
            if (StringUtils.isNotBlank(json2)) {
                // 将 Json 字符串数据转换成集合对象
                return JSON.parseArray(json2, CategoryEntity.class); // json 字符串, 转换类型
            }

            // 2. 远程调用, 查询数据库 并 放入缓存
            ResponseVo<List<CategoryEntity>> categoryResponseVo = pmsClient.queryLevel23CategoriesByPid(pid);
            List<CategoryEntity> categoryEntities = categoryResponseVo.getData();

            if (CollectionUtils.isNotEmpty(categoryEntities)) {
                /**
                 * 缓存雪崩: 由于缓存时间一样, 导致缓存同时失效, 此时大量请求访问这些数据, 请求就会直达数据库, 导致服务器宕机
                 * 　    解决方案: 给缓存时间添加随机值 90 + new Random().nextInt(10)
                 *
                 *      正常数据放入缓存 90 天
                 */
                redisTemplate.opsForValue().set(
                        KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), // k, v
                        90 + new Random().nextInt(10), TimeUnit.DAYS // 缓存时间 90 天
                );
            } else {
                /**
                 * 缓存穿透(数据为空): 大量请求访问不存在的数据, 由于数据不存在, redis 中可能没有, 此时大量请求没有到达数据库, 导致服务器宕机
                 * 　    基础解决方案: 即使为 null 也缓存, 缓存时间一般不超过 5 分钟
                 *       依然存在待解决的问题:  如果每次访问不存在且不重复的数据即使缓存为null 的值 请求依然会直达数据库 应该使用 布隆过滤器
                 */
                redisTemplate.opsForValue().set(
                        KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), // k, v
                        5, TimeUnit.MINUTES // 缓存时间 5 分钟
                );
            }

            return categoryEntities;
        } finally {
            fairLock.unlock();
        }

    }
}
