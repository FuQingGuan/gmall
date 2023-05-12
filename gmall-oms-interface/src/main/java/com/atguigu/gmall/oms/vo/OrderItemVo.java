package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/11 17:57
 * @Email: moumouguan@gmail.com
 */
@Data
public class OrderItemVo {

    private Long skuId;
    private String title;
    private String defaultImage;
    private List<SkuAttrValueEntity> saleAttrs; // 销售属性
    private BigDecimal price; // 实时价格
    private BigDecimal count;
    private Integer weight;
    private List<ItemSaleVo> sales; // 营销信息
    private Boolean store = false; // 库存信息

}
