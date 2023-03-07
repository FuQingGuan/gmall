package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserStatisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统计信息表
 * 
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2023-03-08 04:47:14
 */
@Mapper
public interface UserStatisticsMapper extends BaseMapper<UserStatisticsEntity> {
	
}
