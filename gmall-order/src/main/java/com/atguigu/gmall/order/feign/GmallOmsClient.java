package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/13 04:16
 * @Email: moumouguan@gmail.com
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {

}
