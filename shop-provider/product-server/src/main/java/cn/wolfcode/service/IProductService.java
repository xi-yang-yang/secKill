package cn.wolfcode.service;

import cn.wolfcode.domain.Product;

import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
public interface IProductService {
    List<Product> queryProductByIds(List<Long> ids);
}
