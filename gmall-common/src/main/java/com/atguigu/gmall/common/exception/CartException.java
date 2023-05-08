package com.atguigu.gmall.common.exception;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/8 11:52
 * @Email: moumouguan@gmail.com
 */
public class CartException extends RuntimeException {
    public CartException() {
        super();
    }

    public CartException(String message) {
        super(message);
    }
}
