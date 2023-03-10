package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/3/10 21:46
 * @Email: moumouguan@gmail.com
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
