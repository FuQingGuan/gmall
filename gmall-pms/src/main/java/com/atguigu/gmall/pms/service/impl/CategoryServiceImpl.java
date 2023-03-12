package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 模块名称作为第一位 找到团队的缓存
     * 模型名称作为第二位 找到工程的缓存
     * 真正的key作为第三位 找到真正的值
     */
    private static final String KEY_PREFIX = "INDEX:CATES:"; // 此处只为演示数据一致性效果, PMS 工程未添加缓存 统一操作 INDEX 工程的缓存

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 存在两种请求
     *      1. 查询全部分类
     *          http://api.gmall.com/pms/category/parent/-1
     *              select * from pms_category;
     *      2. 查询某一个分类的子分类
     *          http://api.gmall.com/pms/category/parent/34
     *              select * from pms_category where parent_id = pid;
     * @param pid
     * @return
     */
    @Override
    public List<CategoryEntity> queryCategoriesByPid(Long pid) {
        // 构造查询条件
        LambdaQueryWrapper<CategoryEntity> wrapper = new LambdaQueryWrapper<>();

        // 根据参数判断是否需要拼接查询条件
        if (pid != -1) {
            wrapper.eq(CategoryEntity::getParentId, pid);
        }

        /**
         * 如果 pid 为 -1 则查询全部相当于此处 list(null), wrapper 没有拼接查询条件;
         *      select * from pms_category;
         * 如果 pid 不为 -1 则查询某一个分类下的子分类, 相当于 list(new QueryWrapper<CategoryEntity>().eq("parent_id", pid))
         *      select * from pms_category where parent_id = pid;
         */
        return list(wrapper);
    }

    /**
     * 查询方式:
     *      1. 通过关联查询
     *          SELECT *
     *          FROM pms_category t1
     *          JOIN pms_category t2
     *          ON t1.id = t2.parent_id
     *          WHERE t1.parent_id = 2
     *      2. 分布查询
     *          先查询 二级分类. 遍历二级分类查询三级分类
     *
     * @param pid
     * @return
     */
    @Override
    public List<CategoryEntity> queryLevel23CategoriesByPid(Long pid) {
        return categoryMapper.queryCategoriesByPid(pid);
    }

    @Transactional
    @Override
    public void update(CategoryEntity category) {

        String key = KEY_PREFIX + category.getParentId();

        // 删除 Redis 中相应的缓存内容
        redisTemplate.delete(key);

        // 写入 MySQL
        updateById(category);

        /**
         * 发送消息 异步删除 Redis。方法末尾 方法结束之前发送消息, 归为一个事务. 做到要成功都成功, 要失败都失败. 不能存在 分类修改不成功 消息已发送. 或者 分类修改成功 消息没有发送
         *
         * 交换机: 第一位应该取模块名, 可以方便寻找到自己的交换机. 第二位应该设置为操作信息 操作 SPU, 第三位以 EXCHANGE 结尾
         * rk: 指定内容 单品新增, 更新时应该还有一个 category.update, 删除时应该还有一个 category.delete
         * 消息内容: 首页工程中只目前只缓存了 一级分类的子分类, 以 KEY_PREFIX + pid 做为 Key. 以 其下二三级数据为 Value。此处传递 Key 或者 Pid 即可
         */
        rabbitTemplate.convertAndSend("PMS_CATEGORY_EXCHANGE", "category.update", key);

    }

    @Transactional
    @Override
    public void delete(List<Long> ids) {

        List<CategoryEntity> categoryEntities = list(
                new LambdaQueryWrapper<CategoryEntity>().in(CategoryEntity::getId, ids)
        );

        if (!CollectionUtils.isEmpty(categoryEntities)) {
            CategoryEntity categoryEntity = categoryEntities.get(0);

            System.out.println("categoryEntity = " + categoryEntity);
            String key = KEY_PREFIX + categoryEntity.getParentId();

            // 删除 Redis 中相应的缓存内容
            redisTemplate.delete(key);

            // 写入 MySQL
            removeById(categoryEntity.getId());

            /**
             * 发送消息 异步删除 Redis。方法末尾 方法结束之前发送消息, 归为一个事务. 做到要成功都成功, 要失败都失败. 不能存在 分类修改不成功 消息已发送. 或者 分类修改成功 消息没有发送
             *
             * 交换机: 第一位应该取模块名, 可以方便寻找到自己的交换机. 第二位应该设置为操作信息 操作 SPU, 第三位以 EXCHANGE 结尾
             * rk: 指定内容 单品新增, 更新时应该还有一个 category.update, 删除时应该还有一个 category.delete
             * 消息内容: 首页工程中只目前只缓存了 一级分类的子分类, 以 KEY_PREFIX + pid 做为 Key. 以 其下二三级数据为 Value。此处传递 Key 或者 Pid 即可
             */
            rabbitTemplate.convertAndSend("PMS_CATEGORY_EXCHANGE", "category.delete", key);
        }
    }

}