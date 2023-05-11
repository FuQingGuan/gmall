package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.order.pojo.OrderConfirmVo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/11 21:35
 * @Email: moumouguan@gmail.com
 */
@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("confirm")
    public String confirm(Model model) {
        OrderConfirmVo confirmVo = orderService.confirm();
        model.addAttribute("confirmVo", confirmVo);

        return "trade";
    }


}
