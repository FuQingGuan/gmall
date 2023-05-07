package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/6 18:06
 * @Email: moumouguan@gmail.com
 */
@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;
    private Set<String> attrValue;
}
