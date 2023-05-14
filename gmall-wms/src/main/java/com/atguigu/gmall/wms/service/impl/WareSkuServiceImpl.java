package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    private static final String LOCK_PREFIX = "STOCK:LOCK:";
    private static final String KEY_PREFIX = "STOCK:INFO:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken) {

        // 判空
        if (CollectionUtils.isEmpty(lockVos)) {
            throw new OrderException("请选择要购买的商品");
        }

        // 遍历验库存并锁库存
        lockVos.forEach(this::checkLock);

        // 判断是否存在验库存并锁库存失败的商品, 如果存在则把锁定成功的库存解锁
        if (lockVos.stream().anyMatch(lockVo -> !lockVo.getLock())) {
            // 获取锁定成功的库存, 解锁
            lockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList())
                    .forEach(lockVo ->
                            // 解锁时 不需要 加锁
                            wareSkuMapper.unlock(
                                    lockVo.getWareSkuId(), lockVo.getCount()
                            )
                    );
            // 锁定失败
            return lockVos;
        }

        // 把库存的锁定信息保存到 redis 中，以方便将来解锁库存(用户下单不支付, 或者支付成功减库存)。订单编号作为 key，锁定商品作为 value
        redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVos), 26, TimeUnit.HOURS); // 注意 24 小时不支持就会自动关单 过期时间不得低于 24 小时

        /**
         * 发送延迟消息, 定时解锁库存
         *      此处延迟时间需要大于关单时间. 不能出现订单还未关闭库存先解锁
         */
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.ttl", orderToken);

        // 如果验库存并锁库存成功, 返回 null
        return null;
    }

    /**
     * 使用分布式锁, 验完立刻锁住
     *      如果货物充足 则将 实体类中的锁定状态 lock 设置为 true，反之 设置为 false
     * @param lockVo
     */
    private void checkLock(SkuLockVo lockVo) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockVo.getSkuId()); // 只需要锁住当前商品库存即可
        lock.lock();

        try {

            /**
             * 1. 验库存
             *      本质就是查询数据库，查看那个仓库货物充足
             *
             *      库存数 - 锁定库存 >= 本次购买数量
             *          SELECT * FROM wms_ware_sku WHERE sku_id = 19 AND stock - stock_locked >= 5
             */
            List<WareSkuEntity> wareSkuEntities = wareSkuMapper.check(lockVo.getSkuId(), lockVo.getCount());

            // 如果满足条件的库存为空, 则验证库存失败
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                // 验库存 锁库存失败
                lockVo.setLock(false);
                return;
            }

            // 2. 锁库存: 大数据分析最佳仓库, 这里取第一个仓库
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
            // 锁定仓库 id, 锁定 商品数量
            if (wareSkuMapper.lock(wareSkuEntity.getId(), lockVo.getCount()) == 1) {
                lockVo.setLock(true);
                // 需要记录锁定仓库 id, 如果没有记录仓库 id 用户没有支付就不知道去解锁哪一个仓库了
                lockVo.setWareSkuId(wareSkuEntity.getId());
            }
        } finally {
            // 解锁
            lock.unlock();
        }
    }
}