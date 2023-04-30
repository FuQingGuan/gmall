package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * spu属性值
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2023-04-26 01:50:50
 */
public interface SpuAttrValueService extends IService<SpuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SpuAttrValueEntity> querySearchAttrValueByCidAndSpuId(Long cid, Long spuId);
}

