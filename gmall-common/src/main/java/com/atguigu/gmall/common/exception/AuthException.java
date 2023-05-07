package com.atguigu.gmall.common.exception;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/7 22:29
 * @Email: moumouguan@gmail.com
 */
public class AuthException extends RuntimeException{
    public AuthException() {
        super();
    }

    public AuthException(String message) {
        super(message);
    }
}
