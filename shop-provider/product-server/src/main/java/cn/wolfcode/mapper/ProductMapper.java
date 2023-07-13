package cn.wolfcode.mapper;

import cn.wolfcode.domain.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
public interface ProductMapper {
    List<Product> queryProductByIds(@Param("ids") List<Long> ids);
}
