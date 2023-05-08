package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/7 13:42
 * @Email: moumouguan@gmail.com
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
