package com.atguigu.gmall.pms;

import org.junit.Test;

/**
 * @Description: 事务传播行为伪代码测试
 *      支持当前事物
 *          REQUIRED        支持当前事务，如果不存在，就新建一个
 *          SUPPORTS        支持当前事务，如果不存在，就不使用事务
 *          MANDATORY       支持当前事务，如果不存在，抛出异常
 *
 *      不支持当前事务(挂起当前事务)
 *          REQUIRES_NEW    如果有事务存在，挂起当前事务，创建一个新的事务
 *          NOT_SUPPORTED   以非事务方式运行，如果有事务存在，挂起当前事务
 *          NEVER           以非事务方式运行，如果有事务存在，抛出异常
 *
 *      嵌套事务
 *          NESTED          如果当前事务存在，则嵌套事务执行（嵌套式事务）
 * @Author: Guan FuQing
 * @Date: 2023/3/9 04:37
 * @Email: moumouguan@gmail.com
 */
public class CommunicationBehaviorTest {

    @Test
    public void test() {
//        a(required){
//            b(required);
//            c(requires_new);
//            d(required);
//            e(requires_new);
        // a方法的业务
//        }


        // a 方法的业务出现异常
        // 当 a 方法的业务出现异常时, a b d 都是同一个事务 都会回滚, c e 新建一个事务则不会回滚

        // 当 d 方法出现异常
        // 当 d 方法出现异常, a b d 归属于同一个事务 都会回滚, c 开启一个新的事务不会回滚 e 方法未执行

        // 当 e 方法出现异常
        // 当 e 方法出现异常, e 会回滚 并 向上抛出异常 a() 会回滚 b d 与 a 归属于同一个事务也会回滚, c 开启一个新的事务不会回滚

        // 当 b 方法出现异常
        // 当 b 方法出现异常, 自己会回滚, 并向上抛出异常 a 与 b 归属于同一个事务 a 也会回滚, c d e 方法未执行
    }

    @Test
    public void test2() {

//        a(required){
//            b(required){
//                f(requires_new);
//                g(required)
//            }
//            c(requires_new){
//                h(requires_new)
//                i(required)
//            }
//            d(required);
//            e(requires_new);
        // a方法的业务
//        }

        // a 方法业务出异常
        // a 方法业务出异常, a b g d 归属于同一个事务 会回滚, f c h e 开启新的事务不会回滚 注意 i 是与 c 归属于同一个事务也不会回滚

        // e 方法出异常
        // e 方法出异常, e 会回滚 并 向上抛出异常 a b g d 归属于同一个事务 会回滚, f c h i 开启新的事务不会回滚

        // d 方法出异常
        // d 方法出异常, d 会回滚 并 向上抛出异常 a b g 归属于同一个事务 会回滚, f c h i 开启新的事务不会回滚 e 未执行

        // h , i方法分别出异常
        // h , i方法分别出异常, h 发生异常 a b g h 会回滚 f c 不会回滚 i d e 未执行。i 方法出现异常 a b g c i 会回滚, f h 不会回滚 d e 未执行

        // f , g方法分别出现异常
        // f , g方法分别出现异常, f 发生异常 a b f 会回滚 g c h i d e 未执行。 g 方法 出现异常  a b g 会回滚, f 不会回滚 c h i d e 未执行
    }
}
