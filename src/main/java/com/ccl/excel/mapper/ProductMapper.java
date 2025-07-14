package com.ccl.excel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccl.excel.pojo.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
