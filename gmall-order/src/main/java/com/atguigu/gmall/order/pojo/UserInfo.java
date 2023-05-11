package com.atguigu.gmall.order.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/11 17:55
 * @Email: moumouguan@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private Long userId;

    private String userKey;

    private String userName;

}
