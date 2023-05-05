package com.atguigu.gmall.index.config;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @Description: 布隆过滤器
 *      注: 很少公司会使用布隆过滤器，因为很难驾驭
 *          数据同步问题：
 *              默认数据已经被初始化完成，某天某条数据被删除 布隆过滤器 中不能删 或者 某条数据被修改掉 布隆过滤器 无法进行修改 只能进行新增
 *              (布隆过滤器具有不可变性，无法删除或修改已添加的元素。因此，当数据集合中的某个元素被删除或修改时，对应的布隆过滤器中的信息无法直接更新或删除。)
 *
 *      布隆过滤器 数据同步
 *          1. 定时任务重新生成, 名称一样则会被覆盖 进而 数据就同步了
 *          2. PMS 发送消息给 MQ，index 工程获取消息重新生成一个
 *
 * @Author: Guan FuQing
 * @Date: 2023/5/6 06:41
 * @Email: moumouguan@gmail.com
 */
@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient pmsClient;

    /**
     * 模块名称作为第一位 找到团队的缓存
     * 模型名称作为第二位 找到工程的缓存
     * 真正的key作为第三位 找到真正的值
     */
    private static final String KEY_PREFIX = "INDEX:CATES:";

    @Bean
    public RBloomFilter rBloomFilter(){
        // 初始化布隆过滤器
        RBloomFilter<String> bloomfilter = redissonClient.getBloomFilter("index:bf");
        bloomfilter.tryInit(2000, 0.03);

        // 向 步隆过滤器中初始化数据: 分类 广告
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesByPid(0l);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        if (CollectionUtils.isNotEmpty(categoryEntities)){
            categoryEntities.forEach(categoryEntity ->
                    bloomfilter.add(KEY_PREFIX + categoryEntity.getId().toString())
            );
        }
        return bloomfilter;
    }
}
