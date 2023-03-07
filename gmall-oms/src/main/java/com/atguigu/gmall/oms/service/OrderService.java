package com.atguigu.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.oms.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2023-03-08 04:41:52
 */
public interface OrderService extends IService<OrderEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

