package cn.wolfcode.job;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.redis.RedisKey;
import cn.wolfcode.web.feign.ProductFeignApi;
import cn.wolfcode.web.feign.SeckillProductFeignApi;
import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by lanxw
 */
@Component
@Setter@Getter
@RefreshScope
@Slf4j
public class InitSeckillProductJob implements SimpleJob {
    @Value("${jobCron.initSeckillProduct}")
    private String cron;
    @Autowired
    private SeckillProductFeignApi seckillProductFeignApi;
    @Autowired
    private ProductFeignApi productFeignApi;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void execute(ShardingContext shardingContext) {
        String shardingParameter = shardingContext.getShardingParameter();
        doWork(shardingParameter);
    }

    private void doWork(String time) {
        String listKey = RedisKey.SECKILL_PRODUCT_LIST.getRealKey(time);
        String stockKey = RedisKey.SECKILL_STOCK_COUNT_HASH.getRealKey(time);
        if (redisTemplate.hasKey(listKey)) {
            redisTemplate.delete(listKey);
        }
        if (redisTemplate.hasKey(stockKey)) {
            redisTemplate.delete(stockKey);
        }
        List<SeckillProductVo> seckillProductList = getSeckillProductList(time);
        for (SeckillProductVo vo: seckillProductList) {
            redisTemplate.opsForHash().put(listKey,vo.getId()+"", JSON.toJSONString(vo));
            redisTemplate.opsForHash().put(stockKey,vo.getId()+"", vo.getStockCount()+"");
        }
    }

    private List<SeckillProductVo> getSeckillProductList(String time) {
        Result<List<SeckillProduct>> seckillProductResult = seckillProductFeignApi.queryCurrentlySeckillProduct(Integer.parseInt(time));
        if (seckillProductResult == null || seckillProductResult.hasError()) {
            log.error("秒杀服务繁忙");
            return Collections.emptyList();
        }
        List<SeckillProduct> seckillProductList = seckillProductResult.getData();
        Map<Long,SeckillProduct> seckillProductMap = new HashMap<>();
        for (SeckillProduct seckillProduct : seckillProductList) {
            seckillProductMap.put(seckillProduct.getProductId(),seckillProduct);
        }
        List<Long> ids = new ArrayList(seckillProductMap.keySet());

        Result<List<Product>> productResult = productFeignApi.queryProductByIds(ids);
        if (productResult == null || productResult.hasError()) {
            log.error("商品服务繁忙");
            return Collections.emptyList();
        }
        List<Product> productList = productResult.getData();
        SeckillProduct seckillProduct;
        List<SeckillProductVo> seckillProductVoList = new ArrayList<>();
        SeckillProductVo vo;
        for(Product product: productList){
            seckillProduct = seckillProductMap.get(product.getId());
            vo = new SeckillProductVo();
            BeanUtils.copyProperties(product,vo);
            BeanUtils.copyProperties(seckillProduct,vo);
            seckillProductVoList.add(vo);
        }
        return seckillProductVoList;
    }


}
