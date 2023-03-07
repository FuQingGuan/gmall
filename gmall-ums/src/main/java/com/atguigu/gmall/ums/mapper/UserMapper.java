package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2023-03-08 04:47:14
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
