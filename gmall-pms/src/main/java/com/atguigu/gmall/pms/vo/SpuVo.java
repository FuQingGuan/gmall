package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/3/9 04:07
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