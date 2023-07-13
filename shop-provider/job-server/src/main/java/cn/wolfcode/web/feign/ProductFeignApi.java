package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.web.feign.fallback.ProductFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
@FeignClient(name = "product-service",fallback = ProductFeignFallback.class)
public interface ProductFeignApi {
    @RequestMapping("/product/queryProductByIds")
    public Result<List<Product>> queryProductByIds(@RequestParam("ids") List<Long> ids);
}
