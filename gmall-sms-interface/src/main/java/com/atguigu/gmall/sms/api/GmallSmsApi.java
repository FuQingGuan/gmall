package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/4/27 23:26
 * @Email: moumouguan@gmail.com
 */
public interface GmallSmsApi {

    @PostMapping("sms/skubounds/sales/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo saleVo);
}
