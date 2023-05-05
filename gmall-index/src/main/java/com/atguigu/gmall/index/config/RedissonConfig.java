package com.atguigu.gmall.index.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 单节点 Redisson 配置
 * @Author: Guan FuQing
 * @Date: 2023/5/5 22:32
 * @Email: moumouguan@gmail.com
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        // 可以用"rediss://"来启用SSL连接
        // useSingleServer 单节点
        config.useSingleServer().setAddress("redis://192.168.0.101:6379");
        return Redisson.create(config);
    }
}
