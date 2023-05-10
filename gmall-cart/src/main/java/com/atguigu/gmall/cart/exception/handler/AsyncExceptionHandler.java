package com.atguigu.gmall.cart.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/9 23:26
 * @Email: moumouguan@gmail.com
 */
@Slf4j
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // key 必须是一个固定的 key
    private static final String EXCEPTION_KEY = "CART:EXCEPTION";

    /**
     * 处理未捕获异常方法, 当 异步方法的返回值是非 future 对象时使用
     * @param throwable
     * @param method
     * @param objects
     */
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {

        // TODO: 记录日志 或者 记录到数据库中

        /**
         * 购物车是同步新增 Redis 异步新增 MySQL 的。如果出现异常 没有进行数据同步。MySQL 中的数据可能越差越多 最终失去可分析价值
         *      不可能 Redis 中的所有数据读出来新增到 Redis 效率太低
         *      只需要同步失败数据即可
         *
         *      以谁作为 Key，以谁作为 Value
         *          Key 必须是一个固定的 Key。假设 10 个用户我们使用 userId 作为 Key，定时任务读取的时候 因为 Key 不确定所以要遍历 所有用户，不可取
         *          Value 使用 userId 集合即可。
         *                  因为我们并不确定是 新增、更新 或者 删除 中的哪一种失败。
         *                      新增失败也就是说 Redis 中有数据，MySQL 中没有数据。MySQL 需要新增数据
         *                      更新失败也就是说 Redis 中有数据，MySQL 中也有数据。MySQL 需要更新数量
         *                      删除失败也就是说 Redis 中没有数据，MySQL 中有数据。MySQL 需要删除数据
         *
         *      整体业务实现 Set<CART:EXCEPTION，userId 集合>
         *          当 用户购物车异步失败，通过此处异常处理记录到 Redis 中。定时任务通过 CART:EXCEPTION 进行读取，获取所有 失败用户
         *              删除其 MySQL 中的数据，将 Redis 中的数据同步过去即可，避免各种判断的产生
         */
        // 不可取, 异常处理器只能捕获异步任务的异常。此处是从 ThreadLocal 线程局部变量中获取的，拦截器的线程局部变量跟异步任务线程不是同一个线程
//        UserInfo userInfo = LoginInterceptor.getUserInfo(); // 不可取,
//        redisTemplate.boundSetOps(EXCEPTION_KEY).add(userInfo.getUserId().toString());

        // objects[0].toString() 获取第一个参数, 已经把异步任务的第一个参数都添加 String userId
        redisTemplate.boundSetOps(EXCEPTION_KEY).add(objects[0].toString());

        log.error(
                "异步任务执行失败. 失败信息: {}, 方法: {}, 参数列表: {}",
                throwable.getMessage(), method.getName(), Arrays.asList(objects)
        );
    }
}
