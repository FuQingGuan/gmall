package com.atguigu.gmall.order.pojo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/11 17:57
 * @Email: moumouguan@gmail.com
 */
@Data
public class OrderConfirmVo {

    // 收货地址列表
    private List<UserAddressEntity> addresses;

    // 送货清单, 根据购物车页面传递过来的 skuIds 查询
    private List<OrderItemVo> items;

    // 用户的购物积分信息，ums_member 表中的 integration 字段
    private Integer bounds;

    // 防重的唯一标识. 不允许用户反复提交。保证幂等
    private String orderToken;

}

