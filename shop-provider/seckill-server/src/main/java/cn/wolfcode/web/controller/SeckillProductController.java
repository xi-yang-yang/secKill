package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.ISeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 * 秒杀商品信息查询
 */
@RestController
@RequestMapping("/seckillProduct")
@Slf4j
public class SeckillProductController {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @RequestMapping("/queryCurrentlySeckillProductList")
    public Result<List<SeckillProductVo>> queryCurrentlySeckillProductList(Integer time){
        return Result.success(seckillProductService.queryCurrentlySeckillProductList(time));
    }
    /**
     * 根据前台传入的场次从Redis中查询对应的秒杀商品集合
     *
     * @param time 场次
     * @return
     */
    @RequestMapping("/queryByTime")
    public Result<List<SeckillProductVo>> queryByTime(Integer time) {
        Map<Object, Object> currentCountMap = redisTemplate.opsForHash().entries(SeckillRedisKey.SECKILL_REAL_COUNT_HASH.getRealKey(String.valueOf(time)));
        System.out.println(currentCountMap);
        List<SeckillProductVo> seckillProductVoList = seckillProductService.queryByTime(time);
        int currentCount;
        for(SeckillProductVo vo:seckillProductVoList){
            currentCount = Integer.parseInt((String) currentCountMap.get(String.valueOf(vo.getId())));
            vo.setCurrentCount(currentCount>=0?currentCount:0);
        }
        return Result.success(seckillProductVoList);
    }
    /**
     * 根据传入的场次和秒杀商品ID从Redis中查询对于的秒杀商品详情
     *
     * @param time      场次
     * @param seckillId 秒杀商品ID
     * @return
     */
    @RequestMapping("/find")
    public Result<SeckillProductVo> find(Integer time, Long seckillId) {
        SeckillProductVo vo = seckillProductService.find(time, seckillId);
        String objStr = (String) redisTemplate.opsForHash().get(SeckillRedisKey.SECKILL_REAL_COUNT_HASH.getRealKey(String.valueOf(time)), String.valueOf(seckillId));
        int currentCount = Integer.parseInt(objStr);
        vo.setCurrentCount(currentCount>=0?currentCount:0);
        return Result.success(vo);
    }
}
