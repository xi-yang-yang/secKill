package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.web.feign.fallback.SeckillProductFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
@FeignClient(name = "seckill-service",fallback = SeckillProductFeignFallback.class)
public interface SeckillProductFeignApi {
    @RequestMapping("/seckillProduct/queryCurrentlySeckillProduct")
    public Result<List<SeckillProduct>> queryCurrentlySeckillProduct(@RequestParam("time") Integer time);
}
