package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/11 22:16
 * @Email: moumouguan@gmail.com
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}