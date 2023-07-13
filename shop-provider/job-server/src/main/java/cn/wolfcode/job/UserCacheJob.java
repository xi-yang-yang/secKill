package cn.wolfcode.job;

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
public class UserCacheJob implements SimpleJob {
    @Value("${jobCron.userCache}")
    private String cron;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public void execute(ShardingContext shardingContext) {
        doWork();
    }
    private void doWork() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-7);
        //获取7天前的日期
        Long max = calendar.getTime().getTime();
        String userZSetKey = RedisKey.USER_ZSET.getRealKey("");
        String userHashKey = RedisKey.USER_HASH.getRealKey("");
        Set<String> ids = redisTemplate.opsForZSet().rangeByScore(userZSetKey, 0, max);
        //删除7天前的用户缓存数据
        if(ids.size()>0){
            redisTemplate.opsForHash().delete(userHashKey,ids.toArray());
        }
        redisTemplate.opsForZSet().removeRangeByScore(RedisKey.USER_ZSET.getRealKey(""),0,calendar.getTime().getTime());
    }


}
