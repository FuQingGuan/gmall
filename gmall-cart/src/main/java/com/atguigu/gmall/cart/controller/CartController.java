package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptors.LoginInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/8 11:30
 * @Email: moumouguan@gmail.com
 */
@Controller
public class CartController {

//    @Autowired
//    private LoginInterceptor loginInterceptor;

    @GetMapping("test")
    @ResponseBody
    public String test() {
//    public String test(HttpServletRequest request){

//        System.out.println("Controller 方法执行了" + loginInterceptor.getUserInfo());

//        System.out.println("Controller 方法执行了" + request.getAttribute("userId"));
//        System.out.println("Controller 方法执行了" + request.getAttribute("userKey"));

        System.out.println(LoginInterceptor.getUserInfo());

        return "hello cart!";
    }
}
