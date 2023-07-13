package cn.wolfcode.web.controller;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.redis.CommonRedisKey;
import cn.wolfcode.redis.RedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.DateUtil;
import cn.wolfcode.util.UserUtil;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private IOrderInfoService orderInfoService;
    public static ConcurrentHashMap<Long,Boolean> isStockOverMap = new ConcurrentHashMap();
    @RequestMapping("/doSeckill")
    @RequireLogin
    public Result<String> doSeckill(Integer time,Long seckillId, HttpServletRequest request){
        if(seckillId==null){
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        //判断本地标记.
        Boolean isStockOverSign = isStockOverMap.get(seckillId);
        if(isStockOverSign!=null && isStockOverSign){
            //说明本地标识中对应的秒杀场次的值为true,说明redis中已经减成负数了，说明没有库存了
            return Result.error(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        SeckillProductVo seckillProduct = seckillProductService.find(time, seckillId);
        if(seckillProduct==null){
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        //判断日期是否合法
        /*if(!DateUtil.isLegalTime(seckillProduct.getStartDate(),seckillProduct.getTime())){
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }*/
        String token = request.getHeader(CommonConstants.TOKEN_NAME);
        UserInfo userInfo = UserUtil.getUser(redisTemplate, token);
        if(redisTemplate.opsForHash().hasKey(RedisKey.SECKILL_ORDER_HASH.getRealKey(time+""),seckillId+":"+userInfo.getPhone())){
            //说明已经抢购过了.
            return Result.error(SeckillCodeMsg.REPEAT_SECKILL);
        }
        Long count = redisTemplate.opsForHash().increment(RedisKey.SECKILL_STOCK_COUNT_HASH.getRealKey(time+""),seckillId + "",-1L);
        if(count<0){
            //设置本地标识
            isStockOverMap.put(seckillId,true);
            return Result.error(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        OrderMessage orderMessage = new OrderMessage(time,seckillId,token,userInfo.getPhone());
        rocketMQTemplate.syncSend(MQConstant.ORDER_PEDDING_TOPIC,orderMessage);
        return Result.success("进入队列成功，请等待结果.",null);
    }
    @RequestMapping("/find")
    @RequireLogin
    public Result<OrderInfo> find(String orderNo,HttpServletRequest request){
        String token = request.getHeader(CommonConstants.TOKEN_NAME);
        UserInfo userInfo = UserUtil.getUser(redisTemplate,token);
        //1.传入到orderNo是否有效
        if(StringUtils.isEmpty(orderNo)){
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        OrderInfo orderInfo = orderInfoService.find(orderNo);
        //3.只能访问自己的订单
        if(orderInfo==null || !orderInfo.getUserId().equals(userInfo.getPhone())){
            //说明订单号码有问题，在数据库中无法查询到
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        return Result.success(orderInfo);
    }


}
