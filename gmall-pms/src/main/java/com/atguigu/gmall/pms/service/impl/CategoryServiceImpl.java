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

        /**
         * 使用 LambdaQueryWrapper 还是 QueryWrapper
         *      首先LambdaQueryWrapper 和 QueryWrapper 都是 MyBatis-Plus 中用于构建 SQL 的工具类。
         *      两者的使用方法类似，但是 LambdaQueryWrapper 支持 Lambda 表达式的方式构造查询条件，可以更加简洁明了地构造查询条件，而且支持类型安全检查，大大减少因为错误的拼写或类型不匹配等常见问题导致的错误。
         *          类型安全检查是指在编译期间就可以检查类型是否匹配的能力。
         *              在使用 QueryWrapper 时，需要手动填写字段名称，在填写的过程中有可能犯拼写错误或者写错了字段名称等常见错误。但是这种错误只有在运行时才能发现，并且有可能会导致错误的查询结果。
         *              而使用 LambdaQueryWrapper 可以通过 Lambda 表达式引用实体类中的属性，这样就可以避免手动填写字段名称带来的错误。同时，由于 LambdaQueryWrapper 支持类型安全检查，编译器会在编译期间就检查属性和数据类型是否匹配，这可以在很大程度上减少因为类型不匹配而导致的错误。
         *          此外，当我们修改了表中的字段名时，在 QueryWrapper 中手动修改字段名也是必须的。
         *              db 字段名修改前
         *                  QueryWrapper<User> queryWrapper = new QueryWrapper<>();
         *                  queryWrapper.eq("user_name", "test");
         *              修改后
         *                  queryWrapper.eq("username", "test"); // 需要手动修改每一个正在使用的 username, 如果不修改 就会出现错误的查询结果
         *
         *          而在 LambdaQueryWrapper 中，则可以直接使用实体类中的属性名来构造查询，不需要手动修改字段名。这可以帮助我们避免手动修改时漏掉某些字段名称而导致错误的情况。
         *              db 字段名修改前
         *                  LambdaQueryWrapper<User> lambdaWrapper = new LambdaQueryWrapper<>();
         *                  // 使用了 Lambda 表达式来引用 User 实体类中的属性 userName，避免手动填写字段名称带来的错误，并且不需要手动修改字段名。
         *                  lambdaWrapper.eq(User::getUserName, "test");
         *              修改后
         *                  不需要手动修改代码，LambdaQueryWrapper 可以自动识别属性名称的变化，只需要修改实体类中的属性名 或者 使用 @TableField("字段名") 即可
         *
         *
         *      因此，在实际的开发中，推荐使用 LambdaQueryWrapper 来构造查询条件。
         *      同时，由于 LambdaQueryWrapper 是从 QueryWrapper 扩展而来，因此 LambdaQueryWrapper 也支持 QueryWrapper 的所有功能，可以满足大部分的查询需求。
         */
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