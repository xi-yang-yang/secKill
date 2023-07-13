package cn.wolfcode.redis;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * Created by wolfcode-lanxw
 */
@Getter
public enum RedisKey {
    SECKILL_PRODUCT_LIST("seckillProductList:"),
    SECKILL_STOCK_COUNT_HASH("seckillStockCount:"),
    USER_HASH("userHash"),USER_ZSET("userZset");
    RedisKey(String prefix, TimeUnit unit, int expireTime){
        this.prefix = prefix;
        this.unit = unit;
        this.expireTime = expireTime;
    }
    RedisKey(String prefix){
        this.prefix = prefix;
    }
    public String getRealKey(String key){
        return this.prefix+key;
    }
    private String prefix;
    private TimeUnit unit;
    private int expireTime;
}
