package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/7 22:25
 * @Email: moumouguan@gmail.com
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("toLogin.html")
    public String toLogin(
            @RequestParam(value = "returnUrl", defaultValue = "http://gmall.com") String returnUrl,
            Model model
    ) {

        model.addAttribute("returnUrl", returnUrl);

        return "login";
    }

    @PostMapping("login")
    public String login(
            @RequestParam(value = "returnUrl") String returnUrl,
            @RequestParam(value = "loginName") String loginName,
            @RequestParam(value = "password") String password,
            HttpServletRequest request, HttpServletResponse response
    ) {
        authService.login(loginName, password, request, response);

        // 登陆成功 重定向回到之前路径
        return "redirect:" + returnUrl;
    }
}
