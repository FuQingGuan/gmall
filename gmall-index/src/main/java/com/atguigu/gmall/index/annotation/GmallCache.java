package com.atguigu.gmall.index.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: 自定义注解
 *      我们缓存的逻辑跟 事务注解极为相似
 *          事务注解 Transactional
 *              业务逻辑开始前 开启事务，业务逻辑执行之后 提交或回滚事务
 *          缓存注解 自定义
 *              业务逻辑开始前 查询缓存 添加分布式锁，业务逻辑执行之后 放入缓存 释放分布式锁
 *
 *    自定义前缀 + 请求参数作为 key, 返回结果集作为 value 放入缓存
 *        1. 不同工程不同代码的缓存都应该有自己独有的缓存前缀
 *        			模块名称作为第一位 找到团队的缓存
 *					模型名称作为第二位 找到工程的缓存
 *					真正的key作为第三位 找到真正的值
 *        2. 缓存是有时间限制应该允许自定义缓存时间
 *		  3. 为了防止缓存雪崩, 可以给缓存添加一个随机值
 * 		  4. 为了防止缓存击穿, 给缓存添加分布式锁与缓存前缀后在添加 LOCK 即可
 *		  5. 注此处没有定义 缓存穿透 时即使为空数据也保存的属性因为可以使用 布隆过滤器进行解决
 *
 * @Author: Guan FuQing
 * @Date: 2023/5/5 23:11
 * @Email: moumouguan@gmail.com
 */
@Target({ElementType.METHOD}) // @Target：用于指定注解可以应用的目标元素类型，包括 TYPE（类、接口、枚举）、METHOD（方法）、FIELD（字段）、PARAMETER（参数）等，可以同时指定多个目标类型。
@Retention(RetentionPolicy.RUNTIME) // @Retention：用于指定注解的保留策略，包括 SOURCE（只在源码中保留）、CLASS（在编译时保留，默认值）、RUNTIME（在运行时保留），一般情况下都是使用 RUNTIME 保留注解。
//@Inherited // @Inherited：用于指定注解是否可以被继承，默认情况下注解不会被子类继承，添加了 @Inherited 后可以被继承。
@Documented // @Documented：用于指定注解是否需要包含在 Javadoc 文档中，如果指定了 @Documented，那么注解会被包含在 Javadoc 中。
public @interface GmallCache {

    /**
     * 指定缓存的前缀。默认 gmall:cache:
     * @return
     */
    String prefix() default "gmall:cache:";

    /**
     * 指定缓存过期时间。默认 30 min
     * 单位: min
     * @return
     */
    int timeout() default 30;

    /**
     * 为了防止缓存雪崩, 可以给缓存添加一个随机值。默认 10 min
     * 单位: min
     * @return
     */
    int random() default 10;

    /**
     * 为了防止缓存击穿, 给缓存添加分布式锁
     * 这里可以指定分布式锁的前缀。分布式锁格式: lock 前缀 + 方法形参
     * @return
     */
    String lock() default "gmall:lock:";
}
