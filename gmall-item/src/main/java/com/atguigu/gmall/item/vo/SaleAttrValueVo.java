package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.List;

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
    private List<String> attrValue;
}
