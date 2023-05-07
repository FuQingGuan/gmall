package com.atguigu.gmall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/7 23:03
 * @Email: moumouguan@gmail.com
 */
@Component
public class MyGlobalFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 判断 以及 通用操作
        System.out.println("全局过滤器, 拦截所有经过网关的请求");

        // 放行
        return chain.filter(exchange);
    }
}
