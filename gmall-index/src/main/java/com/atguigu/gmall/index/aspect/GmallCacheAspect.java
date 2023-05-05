package com.atguigu.gmall.index.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @Description: 缓存切面类
 * @Author: Guan FuQing
 * @Date: 2023/5/6 04:57
 * @Email: moumouguan@gmail.com
 */
@Component // @Component 注解用于标记一个类作为 Spring 组件，可以通过 Spring 的自动扫描机制将这些组件加载到 Spring 容器中。通常情况下，使用 @Component 注解来标记需要被 Spring 管理的组件，比如 Service、Repository、Controller 等。使用 @Component 注解的类可以被其他类依赖注入。
@Aspect // @Aspect 注解用于标记一个类为切面类，用于实现 AOP 功能。在一个切面类中，可以定义多个切面方法，每个切面方法可以通过不同的注解来实现不同类型的通知，比如 @Before、@After、@Around 等。切面类可以通过 Spring 的 AOP 代理机制来应用到被增强的目标类上。
public class GmallCacheAspect {

    // 不难发现我们会多次使用一个切点表达式，去给一个或一些方法进行增强，应该使用 @Pointcut 注解将切点表达式进行统一管理. 使用时直接在相应注解上粘贴方法名称即可
    @Pointcut("execution(* com.atguigu.gmall.index.service.*.*(..))")
    public void pointcut() {}

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
    @Before("pointcut()")
    public void before() {
        System.out.println("这是前置通知...");
    }

//    @After("execution(* com.atguigu.gmall.index.service.*.*(..))")
    @After("pointcut()")
    public void after() {
        System.out.println("这是后置通知...");
    }

}
