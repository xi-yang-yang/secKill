package cn.wolfcode.service;


import cn.wolfcode.domain.OrderInfo;

import java.util.Map;

/**
 * Created by wolfcode-lanxw
 */
public interface IOrderInfoService {
    String doSeckill(Integer time,Long seckillId,Long userPhone);

    /**
     * 根据订单编号查询订单
     * @param orderNo
     * @return
     */
    OrderInfo find(String orderNo);

    /**
     * 取消订单
     * @param seckillId
     * @param orderNo
     */
    void cancelOrder(Integer time,Long seckillId, String orderNo);


    /**
     * 支付成功
     * @param params
     * @return
     */
    int paySuccess(Map<String, String> params);

    void refund(OrderInfo orderInfo);

    void payByIntergral(String orderNo, int type);

    void refundByIntergral(OrderInfo orderInfo);
}
