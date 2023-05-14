package com.atguigu.gmall.payment.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/14 13:36
 * @Email: moumouguan@gmail.com
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {

}
