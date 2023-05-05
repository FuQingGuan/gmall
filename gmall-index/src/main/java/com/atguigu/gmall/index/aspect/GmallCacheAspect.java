package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 缓存切面类
 * @Author: Guan FuQing
 * @Date: 2023/5/6 04:57
 * @Email: moumouguan@gmail.com
 */
@Component
// @Component 注解用于标记一个类作为 Spring 组件，可以通过 Spring 的自动扫描机制将这些组件加载到 Spring 容器中。通常情况下，使用 @Component 注解来标记需要被 Spring 管理的组件，比如 Service、Repository、Controller 等。使用 @Component 注解的类可以被其他类依赖注入。
@Aspect
// @Aspect 注解用于标记一个类为切面类，用于实现 AOP 功能。在一个切面类中，可以定义多个切面方法，每个切面方法可以通过不同的注解来实现不同类型的通知，比如 @Before、@After、@Around 等。切面类可以通过 Spring 的 AOP 代理机制来应用到被增强的目标类上。
public class GmallCacheAspect {

    // 不难发现我们会多次使用一个切点表达式，去给一个或一些方法进行增强，应该使用 @Pointcut 注解将切点表达式进行统一管理. 使用时直接在相应注解上粘贴方法名称即可
//    @Pointcut("execution(* com.atguigu.gmall.index.service.*.*(..))")
//    public void pointcut() {}

    /**
     * 切点表达式
     *      * com.atguigu.gmall.index.service.*.*(..))
     *          *                                   返回值为任意类型
     *          com.atguigu.gmall.index.service     该包下
     *          .*                                  任意类
     *          .*                                  任意方法
     *          (..)                                方法的参数为任意
     */
//    @Before("execution(* com.atguigu.gmall.index.service.*.*(..))")

//    @Before("pointcut()")
//    public void before() {
//        System.out.println("这是前置通知...");
//    }

//    @After("execution(* com.atguigu.gmall.index.service.*.*(..))")

//    @After("pointcut()")
//    public void after() {
//        System.out.println("这是后置通知...");
//    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

    /**
     * 自定义缓存注解不需要 指定特定返回值，特定包、特定类、特定方法特定形参列表
     * 可以使用 @annotation + 自定义注解的全类名进行对指定 注解进行增强
     */
    @Pointcut("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public void pointcut() {
    }

    /**
     * 环绕通知
     *      1. 方法必须返回 Object 参数
     *      2. 方法必须有 ProceedingJoinPoint 参数
     *      3. 方法必须抛出 Throwable 异常
     *      4. 必须手动执行目标方法
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录日志开始时间
        long startTime = System.currentTimeMillis();

        /**
         * 在 AOP 中，切点可以选择方法作为连接点。
         *      在这种情况下，可以使用 MethodSignature 接口来获取方法的签名信息，包括方法名、参数类型、返回类型等。
         */
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();

        // 获取方法对象
        Method method = signature.getMethod();

        // 获取方法上的注解
        GmallCache annotation = method.getAnnotation(GmallCache.class);

        // 从连接点中获取方法形参
        Object[] args = joinPoint.getArgs();
        // 获取 注解 缓存 key 前缀
        String keyPrefix = annotation.prefix();
        // 获取 注解 缓存 过期时间
        int timeout = annotation.timeout();
        // 获取 注解 缓存 随机过期时间
        int random = annotation.random();
        // 获取 注解 缓存 锁前缀
        String lockPrefix = annotation.lock();

        // 参数
        String arg = StringUtils.join(args, ",");

        // 组装 key
        String key = keyPrefix + arg;
        // 组装 锁
        String lock = lockPrefix + arg;

        /**
         * 解决缓存穿透
         *      在查询缓存 之前 查询布隆过滤器 判断数据是否存在，不存在则直接返回空。缓存都不需要查询了
         */
        if (!bloomFilter.contains(key)){
            System.err.println("数据不存在, 直接返回");

            return null;
        }

        // 1. 查询缓存 如果缓存命中 则 直接返回
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)) {

            System.err.println(key + " 有缓存，直接命中！");

            getEndTime(startTime, joinPoint, key);

            // 将目标方法的返回值解析为特定的数据类型
            return JSON.parseObject(json, signature.getReturnType());
        }

        // 2. 为了防止缓存击穿, 添加分布式锁
        RLock fairLock = redissonClient.getFairLock(lock);
        fairLock.lock();

        try {
            // 3. 当前请求获取锁的过程中, 可能有其他请求已经把数据放入缓存, 此时, 可以再次查询缓存, 如果命中则直接返回
            String json2 = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(json2)) {

                System.err.println("有其他请求已经把 " + key + " 数据放入缓存，直接命中！");

                getEndTime(startTime, joinPoint, key);

                // 将目标方法的返回值解析为特定的数据类型
                return JSON.parseObject(json, signature.getReturnType());
            }

            // 4. 执行目标方法, 从数据库中获取数据
            Object proceed = joinPoint.proceed(args);

            // 组装 key 的过期时间：缓存超时时间 + 随机值
            long time = timeout + random;
            // 5. 把放入缓存并释放分布式锁
            redisTemplate.opsForValue().set(key, JSON.toJSONString(proceed), time, TimeUnit.MINUTES); // 可以防止缓存雪崩

            System.err.println(key + " 没有缓存，分布式锁 上锁、解锁成功，已把数据放入缓存");

        } finally {
            getEndTime(startTime, joinPoint, key);

            fairLock.unlock();
        }
        return null;
    }

    public void getEndTime(long startTime, ProceedingJoinPoint joinPoint, String key) {
        // 记录结束时间
        long endTime = System.currentTimeMillis();
        // 计算执行时间
        long executeTime = endTime - startTime;
        // 记录执行时间
        System.err.println("方法 " + joinPoint.getSignature().getName() + " " + key + " 共运行 " + executeTime + " ms \n");
    }

}
