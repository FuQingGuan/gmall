package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.pojo.OrderConfirmVo;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/11 21:35
 * @Email: moumouguan@gmail.com
 */
@Service
public class OrderService {

    public OrderConfirmVo confirm() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // 1. 根据当前用户的id 查询收货地址列表
        // 2. 根据当前用户的id 查询已选中的购物车记录: skuId count(最后一次跟用户确认 其他字段应该从数据库实时获取)
        // 3. 根据skuId查询sku
        // 4. 根据skuId查询销售属性
        // 5. 根据skuId查询营销信息
        // 6. 根据skuId查询库存信息
        // 7. 根据当前用户的id查询用户信息

        confirmVo.setAddresses(null);
        confirmVo.setItems(null);
        confirmVo.setBounds(null);
        confirmVo.setOrderToken(null);

        return confirmVo;
    }
}
