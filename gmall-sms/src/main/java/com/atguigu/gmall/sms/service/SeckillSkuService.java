package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SeckillSkuEntity;

import java.util.Map;

/**
 * 秒杀活动商品关联
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2023-04-26 20:12:30
 */
public interface SeckillSkuService extends IService<SeckillSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

