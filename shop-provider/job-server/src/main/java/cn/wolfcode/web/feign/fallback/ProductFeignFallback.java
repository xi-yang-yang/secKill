package cn.wolfcode.web.feign.fallback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.web.feign.ProductFeignApi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
@Component
public class ProductFeignFallback implements ProductFeignApi {
    @Override
    public Result<List<Product>> queryProductByIds(List<Long> ids) {
        return null;
    }
}
