package com.atguigu.gmall.order;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GmallOrderApplicationTests {

    @Autowired
    private GmallUmsClient umsClient;

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

}
