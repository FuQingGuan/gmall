package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;

import java.util.Map;

/**
 * 收货地址表
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2023-03-08 04:47:14
 */
public interface UserAddressService extends IService<UserAddressEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

