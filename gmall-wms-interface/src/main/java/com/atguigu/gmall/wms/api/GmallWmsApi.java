package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/3/11 18:36
 * @Email: moumouguan@gmail.com
 */
public interface GmallWmsApi {

    // es 数据导入 提供远程接口, 3. 根据 skuId 查询对应的 sku 库存信息
    @GetMapping("wms/waresku/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId") Long skuId);

}
