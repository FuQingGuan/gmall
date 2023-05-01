package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/1 12:52
 * @Email: moumouguan@gmail.com
 */
@Data
public class SearchResponseAttrVo {

    private Long attrId;
    private String attrName;
    private List<String> attrValues;

}
