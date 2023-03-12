package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/3/13 01:27
 * @Email: moumouguan@gmail.com
 */
@RestController
public class LockController {

    @Autowired
    private LockService lockService;

    @GetMapping("index/test/lock")
    public ResponseVo testLock() {
        lockService.testLock();

        return ResponseVo.ok();
    }

}
