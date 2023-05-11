package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/11 21:50
 * @Email: moumouguan@gmail.com
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {

}
