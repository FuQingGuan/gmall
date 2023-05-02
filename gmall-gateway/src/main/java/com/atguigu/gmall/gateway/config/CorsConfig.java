package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @Description: 在微服务架构中，网关作为请求的入口，承担了请求路由和服务发现的功能。因此网关更加容易控制和管理请求的流量。
 *      把跨域配置放在网关中，可以统一管理跨域请求，避免每个服务都要单独配置的麻烦。并且，网关可以对跨域请求进行更细致的控制和限制，比如说限制来源域名、请求方法等，进一步增强系统的安全性。
 *      另外，在网关中配置跨域，还可以避免浏览器发送多余的预检请求，提高系统的性能。
 *
 * @Author: Guan FuQing
 * @Date: 2023/4/27 09:17
 * @Email: moumouguan@gmail.com
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        // 初始化 一个 cors 配置类对象
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许跨域访问的域名. * 代表所有域名. 不推荐 1. 存在安全问题 2. 不能携带 cookie
        configuration.addAllowedOrigin("http://manager.gmall.com"); // 可以填写多个
        configuration.addAllowedOrigin("http://192.168.0.121:1000"); // 可以填写多个
        configuration.addAllowedOrigin("http://gmall.com");
        configuration.addAllowedOrigin("http://www.gmall.com");
        // 允许那些请求方式跨域访问 * 允许所有
        configuration.addAllowedMethod("*");
        // 允许携带的头信息 * 允许所有
        configuration.addAllowedHeader("*");
        // 允许 cookie 跨域访问, 需要满足两点 1. AllowedOrigin 不能写 * 2. 此处需要设置为 true
        configuration.setAllowCredentials(true);

        // 初始化 cors 配置源
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        // 注册 cors 配置. /** 针对所有路径 做 cors 配置验证.
        configurationSource.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(configurationSource);
    }
}