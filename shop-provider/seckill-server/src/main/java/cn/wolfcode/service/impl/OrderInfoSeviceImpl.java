package cn.wolfcode.service.impl;



import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.*;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.PayLogMapper;
import cn.wolfcode.mapper.RefundLogMapper;
import cn.wolfcode.redis.RedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.IdGenerateUtil;
import cn.wolfcode.web.feign.IntergralFeignApi;
import cn.wolfcode.web.feign.AlipayPayFeignApi;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by wolfcode-lanxw
 */
@Service
public class OrderInfoSeviceImpl implements IOrderInfoService {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PayLogMapper payLogMapper;
    @Autowired
    private RefundLogMapper refundLogMapper;
    @Autowired
    private AlipayPayFeignApi payFeignApi;
    @Autowired
    private IntergralFeignApi intergralFeignApi;
    @Override
    @Transactional//需要使用事务包装操作原子性
    public String doSeckill(Integer time,Long seckillId,Long userPhone) {
        String orderNo= IdGenerateUtil.get().nextId()+"";
        //往Redis标记这个用户已经下单了.
        String hashKey = RedisKey.SECKILL_ORDER_HASH.getRealKey(time + "");
        String fieldKey = seckillId + ":"+userPhone;
        Boolean success = redisTemplate.opsForHash().putIfAbsent(hashKey,fieldKey, orderNo);
        if(!success){
            throw new BusinessException(SeckillCodeMsg.REPEAT_SECKILL);
        }
        try{
            int count = seckillProductService.decrStock(seckillId);//对应的秒杀场次库存-1
            if(count==0){//返回值（影响行数）==0，说明库存已经没有了
                throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
            }
            this.createOrderInfo(time,seckillId,userPhone,orderNo);//创建商品订单t_order_info
        }catch(Exception e){
            redisTemplate.opsForHash().delete(hashKey,fieldKey);
            throw e;
        }
        return orderNo;
    }

    private void createOrderInfo(Integer time,Long seckillId,Long userId,String orderNo) {
        OrderInfo orderInfo = new OrderInfo();
        SeckillProductVo seckillProductVo = seckillProductService.find(time,seckillId);
        orderInfo.setCreateDate(new Date());//订单创建时间
        orderInfo.setDeliveryAddrId(null);//收货地址id
        orderInfo.setProductCount(1);//购买的商品数量
        orderInfo.setProductId(seckillProductVo.getProductId());//商品id
        orderInfo.setProductImg(seckillProductVo.getProductImg());//商品图片
        orderInfo.setProductName(seckillProductVo.getProductName());//商品名称
        orderInfo.setProductPrice(seckillProductVo.getProductPrice());//商品原价
        orderInfo.setSeckillPrice(seckillProductVo.getSeckillPrice());//商品秒杀价格
        orderInfo.setUserId(userId);//用户
        orderInfo.setOrderNo(orderNo);//订单编号，使用推特雪花算法生成订单号
        orderInfo.setSeckillDate(seckillProductVo.getStartDate());
        orderInfo.setSeckillTime(seckillProductVo.getTime());
        orderInfo.setSeckillId(seckillProductVo.getId());
        orderInfo.setIntergral(seckillProductVo.getIntergral());
        orderInfoMapper.insert(orderInfo);
    }

    @Override
    public OrderInfo find(String orderNo) {
        return orderInfoMapper.find(orderNo);
    }

    @Override
    @Transactional
    public void cancelOrder(Integer time,Long seckillId, String orderNo) {
        //1.根据订单编号，找到订单，判断订单状态
        OrderInfo orderInfo = orderInfoMapper.find(orderNo);
        //2.如果已支付，不做任何逻辑
        //3.如果是未支付
        if(orderInfo.getStatus().equals(OrderInfo.STATUS_ARREARAGE)){
            //     3.1 设置订单状态为 超时取消订单
            int count = orderInfoMapper.updateCancelStatus(orderNo,OrderInfo.STATUS_TIMEOUT);
            if(count==0){
                //在修改订单状态前，已经有其他请求修改了订单的状态.
                throw new  BusinessException(SeckillCodeMsg.CANCEL_ORDER_ERROR);
            }
            //     3.2 真实库存+1
            seckillProductService.incrStock(seckillId);
            //     3.3 取消本地标识
            //     3.4 设置预库存的数量
            seckillProductService.syncRedisStockCount(time,seckillId);
        }
    }

    @Override
    @Transactional
    public int paySuccess(Map<String, String> params) {
        PayLog log = new PayLog();
        String out_trade_no = params.get("out_trade_no");
        log.setOutTradeNo(out_trade_no);
        log.setTradeNo(params.get("trade_no"));
        log.setNotifyTime(params.get("notify_time"));
        log.setTotalAmount(params.get("total_amount"));
        log.setPayType(PayLog.PAY_TYPE_ONLINE);
        payLogMapper.insert(log);
        return orderInfoMapper.changePayStatus(out_trade_no,OrderInfo.STATUS_ACCOUNT_PAID,0);
    }


    @Override
    @Transactional
    public void refund(OrderInfo orderInfo) {
        //查询是否已经退款了
        //输出
        RefundLog log = new RefundLog();
        log.setOutTradeNo(orderInfo.getOrderNo());
        log.setRefundAmount(String.valueOf(orderInfo.getSeckillPrice()));
        log.setRefundReason("就是要退款");
        log.setRefundType(orderInfo.getPayType());
        log.setRefundTime(new Date());
        refundLogMapper.insert(log);

        RefundVo refundVo = new RefundVo();
        BeanUtils.copyProperties(log, refundVo);
        Result<Boolean> result = payFeignApi.refund(refundVo);
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.PAY_SERVER_ERROR);
        }
        if(!result.getData()){
            throw new BusinessException(SeckillCodeMsg.REFUND_ERROR);
        }
        int count = orderInfoMapper.changeRefundStatus(refundVo.getOutTradeNo(), OrderInfo.STATUS_REFUND);
        if(count==0){
            throw new BusinessException(SeckillCodeMsg.REFUND_ERROR);
        }
        //     3.2 真实库存+1
        //  同步库存
        //     3.2 真实库存+1
        seckillProductService.incrStock(orderInfo.getSeckillId());
        //     3.3 取消本地标识
        //     3.4 设置预库存的数量
        seckillProductService.syncRedisStockCount(orderInfo.getSeckillTime(),orderInfo.getSeckillId());
    }
    @Override
    @GlobalTransactional
    public void refundByIntergral(OrderInfo orderInfo) {
        //查询是否已经退款了
        RefundLog log = new RefundLog();
        log.setOutTradeNo(orderInfo.getOrderNo());
        log.setRefundAmount(String.valueOf(orderInfo.getSeckillPrice()));
        log.setRefundReason("就是要退款");
        log.setRefundType(orderInfo.getPayType());
        log.setRefundTime(new Date());
        refundLogMapper.insert(log);

        OperateIntergralVo operateIntergralVo = new OperateIntergralVo();
        operateIntergralVo.setUserId(orderInfo.getUserId());
        operateIntergralVo.setPk(orderInfo.getOrderNo());
        operateIntergralVo.setValue(orderInfo.getIntergral());
        operateIntergralVo.setInfo("退款增加积分");
        Result result = intergralFeignApi.incrIntergral(operateIntergralVo);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int i = 1/0;
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.INTERGRAL_SERVER_ERROR);
        }
        int count = orderInfoMapper.changeRefundStatus(orderInfo.getOrderNo(), OrderInfo.STATUS_REFUND);
        if(count==0){
            throw new BusinessException(SeckillCodeMsg.REFUND_ERROR);
        }
        //     3.2 真实库存+1
        //  同步库存
        //     3.2 真实库存+1
        seckillProductService.incrStock(orderInfo.getSeckillId());
        //     3.3 取消本地标识
        //     3.4 设置预库存的数量
        seckillProductService.syncRedisStockCount(orderInfo.getSeckillTime(),orderInfo.getSeckillId());
    }
    @GlobalTransactional
    @Override
    public void payByIntergral(String orderNo, int type) {
        System.out.println("支付...."+new Date());
        OrderInfo orderInfo = this.find(orderNo);
        if(orderInfo==null){
           throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        OperateIntergralVo operateIntergralVo = new OperateIntergralVo();
        operateIntergralVo.setUserId(orderInfo.getUserId());
        operateIntergralVo.setPk(orderInfo.getOrderNo());
        operateIntergralVo.setValue(orderInfo.getIntergral());
        operateIntergralVo.setInfo("订单扣除积分");
        Result result = intergralFeignApi.decrIntergral(operateIntergralVo);
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.INTERGRAL_SERVER_ERROR);
        }
        PayLog log = new PayLog();
        String out_trade_no = orderInfo.getOrderNo();
        log.setOutTradeNo(out_trade_no);
        log.setTradeNo("");
        log.setPayType(PayLog.PAY_TYPE_ONLINE);
        log.setNotifyTime(new Date().toString());
        log.setTotalAmount(String.valueOf(orderInfo.getIntergral()));
        payLogMapper.insert(log);
        orderInfoMapper.changePayStatus(out_trade_no,OrderInfo.STATUS_ACCOUNT_PAID,type);

    }
}
