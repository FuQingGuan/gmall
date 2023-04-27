package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description: spu 扩展对象
 *      包含：spu 基本信息、spuImages 图片信息、baseAttrs 基本属性信息、skus 信息
 *
 * @Author: Guan FuQing
 * @Date: 2023/4/27 22:55
 * @Email: moumouguan@gmail.com
 */
@Data
public class SpuVo extends SpuEntity {

    // 图片信息
    private List<String> spuImages;

    // 基本属性信息
    private List<SpuAttrValueVo> baseAttrs;

    // sku 信息
    private List<SkuVo> skus;


}
