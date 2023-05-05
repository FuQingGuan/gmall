package com.atguigu.gmall.index.service;

import com.atguigu.gmall.index.utils.DistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private RedissonClient redissonClient;

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
     *
     *      存在的问题
     *          1. 死锁
     *              一个线程获取到锁 还没有执行到释放锁操作 服务器宕机. 其他线程获取不到锁 即使 服务器重启 这把锁也无法被释放掉. 其他线程一直执行递归操作 最终导致服务器资源耗尽而宕机
     *                  添加过期时间在 set(获取锁) 时去设置过期时间
     *
     *              不可重入可能会导致死锁
     *                  不可重入导致的死锁是指一个线程已经获取了锁，在没有释放锁的情况下再次请求获取锁会导致死锁。
     *                  通俗地说，一个线程在持有锁的情况下，再次去获取锁的时候会被自己给阻塞住，这样就无法继续执行，最终导致死锁。
     *
     *                  可重入锁: hash Map<lockName, Map<uuid, 重入次数>>
     *                     可重入加锁
     *                         1. 判断锁是否存在(exists), 如果不存在(0) 则直接获取锁(hset)
     *                         2. 判断是否自己的锁(hexists), 如果是(1)则重入(hincrby)
     *                         3. 否则获取锁失败, 返回 0
     *                     可重入解锁
     *                         1. 判断自己的锁是否存在（hexists），如果不存在（0）则返回nil
     *                         2. 如果自己的锁存在，则直接减1（hincrby -1），并判断减1后的值是否为0，为0则直接释放锁（del） 返回1
     *                         3. 直接返回 0, 表示出一次
     *
     *          2. 防误删
     *              如果业务逻辑的执行时间是7s, A 服务获取锁 业务没有执行完 锁3秒被自动释放, B 服务获取到锁 业务没有执行完 锁3秒被自动释放, C 服务获取锁执行业务逻辑.
     *              A 服务业务执行完成 释放锁, 这时释放的是 C 的锁. 导致 C 业务只执行了 1s 就被别人释放. 最终等于没有锁(可能会释放其他服务器的锁)
     *                  setnx 获取锁时, 设置一个指定的唯一值(例如：uuid); 释放前获取这个值, 判断是否自己的锁(注意删除缺乏原子性)
     *
     *          3. 保证删除的原子性
     *              A 服务执行删除时 查询到 lock 值确实与 uuid 相等. A 服务删除前 lock 刚好过期时间已到 被 redis 释放. B 服务获取了锁 A 服务把 B 服务的锁释放 最终等于没有锁
     *                  判断与删除间也要保证原子性, 使用 lua 脚本保证删除的原子性
     *                      在 redis 中对lua 脚本提供了主动支持: 打印的不是 lua 脚本的 print 而是 lua 脚本的返回值
     *                          eval script numkeys key [key ...] arg [arg ...]
     *                              eval: 指令名称
     *                              script: lua 脚本字符串
     *                              numkeys: key 列表的元素数量. 必须参数
     *                              key: 传递的 key 列表. keys[index] 下标从 1 开始的
     *                              arg: 传递的 arg 列表. 同上
     *                          变量
     *                              全局变量: a = 5 redis 中的 lua 脚本不支持全局变量
     *                              局部变量: local a = 5
     *                      redis 给 lua 脚本提供了一个类库: redis.call()
     *              Lua 脚本可以保证原子性，因为 Redis 在执行 Lua 脚本时，会将整个脚本作为一个整体执行，Redis 会将脚本编译成字节码，然后再在一个隔离的环境中运行。在这个运行环境中，脚本会被当作一个 Redis 命令来执行，且这个命令是以原子方式执行的，它要么全部执行成功，要么全部执行失败。
     *              在执行 Lua 脚本期间，Redis 会将脚本转换成一个 Redis 命令，并将其原子地发送到 Redis 服务器执行。在执行过程中，Redis 会禁止其他客户端对相同的 key 进行读写操作，以确保执行脚本期间的原子性。因此，如果多个客户端同时执行相同的 Lua 脚本，只有一个客户端能够成功执行，其他客户端会失败并返回相应的错误信息。这样就保证了原子性。
     *
     *          4. 自动续期: 定时器 + lua 脚本(需要判断是否是自己的锁)
     *              A线程超时时间设为10s(为了解决死锁问题), 但代码执行时间可能需要30s, 然后 redis 服务端 10s 后将锁删除
     *              此时, B线程恰好申请锁, redis 服务端不存在该锁, 可以申请, 也执行了代码
     *              那么问题来了, A、B线程都同时获取到锁并执行业务逻辑, 这与分布式锁最基本的性质相违背：在任意一个时刻，只有一个客户端持有锁（即独享排他）
     *
     *                  判断是否是自己的锁, 是 则重制过期时间
     *
     *          单点故障 集群(主从): redLock 算法解决 Reddison
     *              1. 客户端程序从主中获取到锁
     *              2. 从还没有来得及同步数据, 主挂了
     *              3. 从升级为新主
     *              4. 其他客户端程序从新主中获取到锁. 导致锁机制失效
     */
    public void testLock() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock();

        try {
            String number = redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)) {
                redisTemplate.opsForValue().set("number", "1");
            }
            int num = Integer.parseInt(number);
            redisTemplate.opsForValue().set("number", String.valueOf(++num));
        } finally {
            lock.unlock();
        }
    }

    public void testLock3() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = distributedLock.tryLock("lock", uuid, 30);

        if (lock) {
            try {
                // 查询 redis 中的 num 值
                String number = redisTemplate.opsForValue().get("number");
                // 没有该值设置默认值
                if (StringUtils.isBlank(number)) {
                    redisTemplate.opsForValue().set("number", "1");
                }
                // 有值转换成 int
                int num = Integer.parseInt(number);
                // 把 redis 中的 num 值 +1
                redisTemplate.opsForValue().set("number", String.valueOf(++num));

//                testSubLock(uuid);

                // 自动续期测试
                try {
                    TimeUnit.SECONDS.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } finally {
                distributedLock.unLock("lock", uuid);
            }
        }
    }

    public void testSubLock(String uuid) {
        distributedLock.tryLock("lock", uuid, 30);
        System.out.println("==========================");
        distributedLock.unLock("lock", uuid);
    }


    public void testLock2() {

        String uuid = UUID.randomUUID().toString();

        /**
         * 加锁 setIfAbsent 类似与 setNx 当 key 不存在即设置成功 否 则 失败
         *      分布式锁本质就是对 key 的争抢, 谁先设置成功谁就先获取锁
         */
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS); // 解决死锁 添加过期时间在 set(获取锁) 时, 去设置过期时间;

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

            // 添加过期时间 （缺乏原子性：如果在 setnx 和 expire 之间出现异常，锁也无法释放)
//            redisTemplate.expire("lock", 3, TimeUnit.SECONDS);

            String number = redisTemplate.opsForValue().get("number");

            if (StringUtils.isBlank(number)) {
                redisTemplate.opsForValue().set("number", "1");
            }

            int num = Integer.parseInt(number);

            redisTemplate.opsForValue().set("number", String.valueOf(++num));

            testSubLock(); // 测试可重入性

            // 判断 redis 中 lock 值是否跟当前 uuid 一致, 如果一致则执行 del 指令
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then " +
                    "   return redis.call('del', KEYS[1])" +
                    "else " +
                    "   return 0 " +
                    "end";

            // execute 可以接受 lua 脚本, 传入的脚本字符串(脚本字符, 返回类型), key 列表， arg 列表
            redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);

//            if (StringUtils.equals(redisTemplate.opsForValue().get("lock"), uuid)) { // 解锁时判断是否是自己的锁
//                // 释放锁
//                redisTemplate.delete("lock");
//            }
        }
    }

    /**
     * 不可重入导致的死锁
     */
    public void testSubLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS); // 解决死锁 添加过期时间在 set(获取锁) 时去设置过期时间

        // 一顿操作
        if (!flag) {
            try {
                // 睡眠一段时间 让抢到锁的线程执行业务逻辑 减少竞争
                Thread.sleep(30);
                // 设置锁(加锁)失败重新调用该方法进行重试
                testSubLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then " +
                "   return redis.call('del', KEYS[1])" +
                "else " +
                "   return 0 " +
                "end";

        redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
    }
}