package com.atguigu.gmall.cart.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/8 11:37
 * @Email: moumouguan@gmail.com
 */
@Data
@TableName("cart_info")
public class Cart {

    @TableId
    private Long id;
    @TableField("user_id")
    private String userId;
    @TableField("sku_id")
    private Long skuId;
    @TableField("`check`") // check是mysql的关键字，所以这里要加'`'号
    private Boolean check; // 选中状态
    private String defaultImage;
    private String title;
    @TableField("sale_attrs")
    private String saleAttrs; // 销售属性：List<SkuAttrValueEntity>的json格式
    private BigDecimal price; // 加入购物车时的价格
    private BigDecimal count;
    private Boolean store = false; // 是否有货
    private String sales; // 营销信息: List<ItemSaleVo>的json格式

    @TableField(exist = false)
    private BigDecimal currentPrice; // 实时价格
}
