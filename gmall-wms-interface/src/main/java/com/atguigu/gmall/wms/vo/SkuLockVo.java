package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/13 00:46
 * @Email: moumouguan@gmail.com
 */
@Data
public class SkuLockVo {

    private Long skuId; // 锁定的商品id
    private Integer count; // 购买的数量
    private Boolean lock; // 锁定状态, 传递时没有 响应时返回
    private Long wareSkuId; // 锁定成功时，锁定的仓库id
    private String orderToken; // 方便以订单为单位缓存订单的锁定信息


}
