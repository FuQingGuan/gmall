package com.atguigu.gmall.sms.vo;

import lombok.Data;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/6 18:06
 * @Email: moumouguan@gmail.com
 */
@Data
public class ItemSaleVo {

    private Long saleId; // 营销 id
    // 积分、满减、打折
    private String type; // 营销 类型
    private String desc; // 营销 描述

}
