package com.atguigu.gmall.pms;

import org.junit.Test;

/**
 * @Description: 事务伪代码练习
 *      REQUIRED：一个事务，要么成功，要么失败
 *      REQUIRES_NEW：两个不同事务，彼此之间没有关系。一个事务失败了不影响另一个事务
 *
 * @Author: Guan FuQing
 * @Date: 2023/4/28 03:00
 * @Email: moumouguan@gmail.com
 */
public class TransactionalPseudocodeTest {

    @Test
    public void test() {

//        a (required) {
//            b(required);
//            c(required_new);
//            d(required);
//            e(required_new);
//            a 业务
//        }

        // a 业务发生异常
        // a 业务发生异常 a、b、d 回滚, c、e 不回滚

        // e 发生异常
        // e 发生异常。a、b、d、e 归属于同一个事务回滚, c 不回滚

        // d 发生异常
        // d 发生异常。a、b、d 归属于同一个事务回滚, c 不回滚, e 不执行

        // c 发生异常
        // c 发生异常。a、b、c 会回滚, d、e 未执行
    }

    @Test
    public void test2() {

//        a(required) {
//            b(required) {
//                f(required_new);
//                g(required);
//            }
//            c(required_new) {
//                h(required_new);
//                i(required);
//            }
//            d(required);
//            e(required_new);
        // a 业务
//        }

        // a 业务异常
        // a 业务异常。a、b、g、d 会回滚, f、c、h、i、e 不会滚

        // i 出现异常
        // i 出现异常。a、b、g、c、i 会回滚。i 跟 c 是同一个事务, 所以会回滚。c 方法异常 a 方法肯定也有异常 所以 a、b、g 也会回滚

        // h 出现异常
        // h 出现异常。h 发生异常不会影响到 c，进而不会影响到 a。所以当 h 发生异常时只有 h 本身进行回滚
    }
}
