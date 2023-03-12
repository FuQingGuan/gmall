package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/3/13 00:20
 * @Email: moumouguan@gmail.com
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
