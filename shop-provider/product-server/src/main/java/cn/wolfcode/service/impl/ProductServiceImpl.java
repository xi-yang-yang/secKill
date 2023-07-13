package cn.wolfcode.service.impl;

import cn.wolfcode.domain.Product;
import cn.wolfcode.mapper.ProductMapper;
import cn.wolfcode.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by lanxw
 */
@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;
    @Override
    public List<Product> queryProductByIds(List<Long> ids) {
        if(ids==null || ids.size()==0){
            //没有传参数过来，没必要去查询数据库，直接返回空集合
            return Collections.EMPTY_LIST;
        }
        return productMapper.queryProductByIds(ids);
    }
}
