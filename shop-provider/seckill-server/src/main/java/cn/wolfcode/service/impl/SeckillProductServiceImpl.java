package cn.wolfcode.service.impl;

import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mapper.SeckillProductMapper;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.redis.RedisKey;
import cn.wolfcode.service.ISeckillProductService;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
@Service
public class SeckillProductServiceImpl implements ISeckillProductService {
    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Override
    public int decrStock(Long seckillId) {
        return seckillProductMapper.decrStock(seckillId);
    }
    @Override
    public void incrStock(Long seckillId) {
        seckillProductMapper.incrStock(seckillId);
    }
    @Override
    public void syncRedisStockCount(Integer time,Long seckillId) {
        //从数据库中查询秒杀商品的库存
        int stockCount = seckillProductMapper.getStockCount(seckillId);
        if(stockCount>0){
            //说明数据库中还有库存
            //1.取消本地标识(发布订阅模式)
            rocketMQTemplate.syncSend(MQConstant.CANCEL_SECKILL_OVER_SIGE_TOPIC,seckillId);
            //2.设置redis中预库存数量.
            redisTemplate.opsForHash().put(RedisKey.SECKILL_STOCK_COUNT_HASH.getRealKey(time+""),String.valueOf(seckillId),String.valueOf(stockCount));
        }
    }
    @Override
    public List<SeckillProduct> queryCurrentlySeckillProduct(Integer time) {
        return seckillProductMapper.queryCurrentlySeckillProduct(time);
    }
    @Override
    public List<SeckillProductVo> queryByTime(Integer time) {
        //从Redis中查询秒杀商品集合
        String key = RedisKey.SECKILL_PRODUCT_LIST.getRealKey(time + "");
        List<Object> values = redisTemplate.opsForHash().values(key);
        List<SeckillProductVo> resultList = new ArrayList<>();
        for(Object value:values){
            resultList.add(JSON.parseObject((String) value, SeckillProductVo.class));
        }
        return resultList;
    }

    @Override
    public SeckillProductVo find(Integer time, Long seckillId) {
        //从Redis中查询秒杀商品
        String key = RedisKey.SECKILL_PRODUCT_LIST.getRealKey(time + "");
        String jsonObj = (String) redisTemplate.opsForHash().get(key, seckillId + "");
        System.out.println(jsonObj);
        return JSON.parseObject(jsonObj,SeckillProductVo.class);
    }
}
