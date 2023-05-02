package com.atguigu.gmall.pms.listener;

import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/3 04:02
 * @Email: moumouguan@gmail.com
 */
@Component // 注入到 Spring 容器中
public class CategoryListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 该方法会声明此方法是一个监听器, 可以监听队列获取消息
    @RabbitListener(bindings = @QueueBinding( // 声明绑定关系
            // 绑定的 队列, 将下方声明的交换机绑定给此队列
            value = @Queue(value = "PMS_CATEGORY_QUEUE",
                    durable = "true", // 使用 durable 指定是否需要持久化 默认是 true
                    // 队列存在的情况下 如果声明一个属性与之前队列不一样 rabbitmq 就会报声明错误, ignoreDeclarationExceptions 可以使用忽略声明异常进行忽略(可以忽略声明异常使用既有的)
                    ignoreDeclarationExceptions = "true"),  // 默认队列不需要设置
            // 需要与生产者中的交换机一致, type 交换机类型
            exchange = @Exchange(value = "PMS_CATEGORY_EXCHANGE", ignoreDeclarationExceptions = "ture", // 通常在 交换机中设置忽略声明异常, 可以避免重复声明
                    // 通配模型
                    type = ExchangeTypes.TOPIC), // 绑定的 交换机
//            key = {"category.update, category.delete"} // rk, 可以绑定多个
            key = {"category.*"} // rk, 可以绑定多个
    ))
    public void syncData(String key, Channel channel, Message message) throws IOException {

        long startTime = System.currentTimeMillis();
        System.err.println("异步删除缓存开始: " + startTime + "ms");

        // 如果为空表示为垃圾消息直接确认
        if (StringUtils.isBlank(key)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        try {
            // 异步删除。避免 在删除 Redis 到写入 MySQL间. 有用户 查询 Redis 发现没有数据 将旧数据 放入 Redis 导致的数据不一致
            redisTemplate.delete(key);

            // 确认消息: 游标 直接 copy, 是否批量确认消息: 设置为 true 的话 从最近确认消息开始到当前消息之间未被确认的消息都被批量确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();

            // 判断消息是否重试投递过
            if (message.getMessageProperties().getRedelivered()) { // 已重试过直接拒绝
                // TODO: 记录日志, 或者保存到数据库表中 通过定时任务, 或者 人工排查 处理消息. 如果有死信队列会进入死信队列

                // 拒绝消息: 游标, 重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else { // 未重试过, 重新入队
                // 不确认消息: 游标, 是否批量确认, 重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }

        long endTime = System.currentTimeMillis(); // 商品信息同步结束
        System.err.println("异步删除缓存结束: " + startTime + "ms");
        System.err.println("异步删除缓存结束! 总耗时: " + (endTime - startTime) + "ms");
    }

}
