package com.atguigu.gmall.order;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.feign.GmallCartClient;
import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GmallOrderApplicationTests {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Test
    void contextLoads() {
    }

    // order 1. 根据当前用户的id 查询收货地址列表
    @Test
    public void test() {
        ResponseVo<List<UserAddressEntity>> addressesResponseVo = umsClient.queryAddressesByUserId(4L);
        List<UserAddressEntity> addressEntities = addressesResponseVo.getData();

        System.out.println("addressEntities = " + addressEntities);
    }

    // order 2. 根据当前用户的id 查询已选中的购物车记录
    @Test
    public void test2() {
        ResponseVo<List<Cart>> cartsResponseVo = cartClient.queryCheckedCartsByUserId(4L);
        List<Cart> carts = cartsResponseVo.getData();

        System.out.println("carts = " + carts);
    }

    // order 3. 根据skuId查询sku
    @Test
    public void test3() {
        ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(12L);
        SkuEntity skuEntity = skuEntityResponseVo.getData();

        System.out.println("skuEntity = " + skuEntity);
    }
}
