package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/10 12:47
 * @Email: moumouguan@gmail.com
 */
@Component
public class CartListener {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "CART:INFO:";

    private static final String PRICE_PREFIX = "CART:PRICE:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART_PRICE_QUEUE"),
            exchange = @Exchange(value = "PMS_SPU_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.update"}
    ))
    public void syncPrice(Long spuId, Message message, Channel channel) throws IOException {

        if (spuId == null) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 根据 spuId 查询 spu 下的 sku 集合, 同步价格
        ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkuBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();

        if (CollectionUtils.isEmpty(skuEntities)) {
            // 可能该 spu 没有 sku 直接确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

        // 遍历 sku 集合 同步价格
        skuEntities.forEach(skuEntity -> {
            // setIfPresent 如果存在才设置
            redisTemplate.opsForValue().setIfPresent(PRICE_PREFIX + skuEntity.getId(), skuEntity.getPrice().toString());
        });

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART_DELETE_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public void deleteCart(Map<String, Object> msg, Message message, Channel channel) throws IOException {

        if (org.springframework.util.CollectionUtils.isEmpty(msg)) {
            // 垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 获取消息中的 userId 与 skuIds
        String userId = msg.get("userId").toString();
        String skuIdsJson = msg.get("skuIds").toString();

        if (StringUtils.isBlank(userId) && StringUtils.isBlank(skuIdsJson)) {
            // 如果两者任意为空都是垃圾消息直接确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        List<String> skuIds = JSON.parseArray(skuIdsJson, String.class);

        // 删除该用户对应的购物车记录
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        hashOps.delete(skuIds.toArray());
        // mysql 中的购物车也要删除
        cartMapper.delete(
                new LambdaUpdateWrapper<Cart>().eq(Cart::getUserId, userId).in(Cart::getSkuId, skuIds)
        );

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
