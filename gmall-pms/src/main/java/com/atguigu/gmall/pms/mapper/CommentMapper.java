package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CommentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价
 * 
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2023-03-08 04:39:38
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
	
}
