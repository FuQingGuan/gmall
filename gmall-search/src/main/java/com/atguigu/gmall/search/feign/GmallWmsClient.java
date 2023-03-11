package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/3/11 18:37
 * @Email: moumouguan@gmail.com
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {

}
