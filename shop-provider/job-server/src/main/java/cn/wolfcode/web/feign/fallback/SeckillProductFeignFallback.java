package cn.wolfcode.web.feign.fallback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.web.feign.SeckillProductFeignApi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
@Component
public class SeckillProductFeignFallback implements SeckillProductFeignApi {
    @Override
    public Result<List<SeckillProduct>> queryCurrentlySeckillProduct(Integer time) {
        return null;
    }
}
