package cn.wolfcode.mapper;

import cn.wolfcode.domain.SeckillProduct;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
public interface SeckillProductMapper {
    List<SeckillProduct> queryCurrentlySeckillProduct(Integer time);
    int decrStock(Long seckillId);
    int incrStock(Long seckillId);
    int getStockCount(Long seckillId);
}
