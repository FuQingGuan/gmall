package com.atguigu.gmall.wms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品库存
 *
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2023-04-26 20:21:41
 */
@Api(tags = "商品库存 管理")
@RestController
@RequestMapping("wms/waresku")
public class WareSkuController {

    @Autowired
    private WareSkuService wareSkuService;

    // 提交订单 验库存 锁库存
    @PostMapping("check/lock/{orderToken}")
    public ResponseVo<List<SkuLockVo>> checkAndLock(@RequestBody List<SkuLockVo> lockVos, @PathVariable("orderToken") String orderToken){
        List<SkuLockVo> skuLockVOS = wareSkuService.checkAndLock(lockVos, orderToken);
        return ResponseVo.ok(skuLockVOS);
    }

    /**
     * baseCrud: 6. 根据 skuId 查询对应的 sku 库存信息
     * es 数据导入 提供远程接口, 3. 根据 skuId 查询对应的 sku 库存信息
     * 商品详情页 7. 根据 skuId 查询 sku 的库存信息
     * order 5. 根据skuId查询营销信息
     *
     * 　请求路径
     * 　　　　http://api.gmall.com/wms/waresku/sku/1
     * 　　　　　　　　　　　　　　　　/wms/waresku/sku/{skuId}
     *
     * @param skuId
     * @return
     */
    @GetMapping("/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId") Long skuId) {
        List<WareSkuEntity> wareSkuEntities = wareSkuService.list(
                new LambdaQueryWrapper<WareSkuEntity>().eq(WareSkuEntity::getSkuId, skuId)
        );

        return ResponseVo.ok(wareSkuEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryWareSkuByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = wareSkuService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<WareSkuEntity> queryWareSkuById(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return ResponseVo.ok(wareSku);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		wareSkuService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
