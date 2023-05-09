package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptors.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/8 11:30
 * @Email: moumouguan@gmail.com
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 更新购物车数量
     * @param cart
     * @return
     */
    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart) {
        cartService.updateNum(cart);

        return ResponseVo.ok();
    }

    /**
     * 更新购物车状态
     * @param cart
     * @return
     */
    @PostMapping("updateStatus")
    @ResponseBody
    public ResponseVo updateStatus(@RequestBody Cart cart) {
        cartService.updateStatus(cart);

        return ResponseVo.ok();
    }

    @GetMapping("cart.html")
    public String queryCarts(Model model) {
        List<Cart> carts = cartService.queryCarts();

        model.addAttribute("carts", carts);
        return "cart";
    }

    /**
     * 添加购物车成功, 重定向到购物车成功页
     *
     * @param cart
     * @return
     */
    @GetMapping
    public String saveCart(Cart cart) {

        long now = System.currentTimeMillis();
        System.out.println("新增购物车 方法开始执行！");

        if (cart == null || cart.getSkuId() == null) {
            throw new RuntimeException("没有选择添加到购物车的商品信息！");
        }

        cartService.saveCart(cart);

        System.out.println("新增购物车 方法结束执行！！！" + (System.currentTimeMillis() - now));

        // 重定向到新增成功页
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId() + "&count=" + cart.getCount();
    }

    /**
     * 新增成功跳转新增成功页
     * @return
     */
    @GetMapping("addCart.html")
    public String queryCart(Cart cart, Model model) {
        // 覆盖前取出当前传入的 count
        BigDecimal count = cart.getCount();
        // 返回的是购物车对象, 对传入的参数进行覆盖
        cart = cartService.queryCartBySkuId(cart.getSkuId());
        // 覆盖前的 count 设置进去
        cart.setCount(count);

        model.addAttribute("cart", cart);

        return "addCart";
    }

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
