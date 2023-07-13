package cn.wolfcode.service;

import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
public interface ISeckillProductService {
    /**
     * 根据场次从数据库中查询当天的秒杀商品集合
     * @param time 场次
     * @return
     */
    List<SeckillProduct> queryCurrentlySeckillProduct(Integer time);

    /**
     * 根据场次从Redis中查询当天的秒杀商品集合
     * @param time 场次
     * @return
     */
    List<SeckillProductVo> queryByTime(Integer time);

    /**
     * 根据场次和秒杀商品ID从Redis中查询秒杀商品详情
     * @param time
     * @param seckillId
     * @return
     */
    SeckillProductVo find(Integer time, Long seckillId);

    /**
     * 根据秒杀商品ID从数据库中进行减库存-1操作
     * @param seckillId
     * @return
     */
    int decrStock(Long seckillId);
    /**
     * 根据秒杀商品ID从数据库中进行加库存+1操作
     * @param seckillId
     * @return
     */
    void incrStock(Long seckillId);
    /**
     * 根据场次和秒杀商品id查询数据库中的实际库存，然后同步到Redis中
     * @param seckillId
     * @return
     */
    void syncRedisStockCount(Integer time,Long seckillId);

}
