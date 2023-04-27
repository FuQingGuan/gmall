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
import org.springframework.stereotype.Service;

import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

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

}