package cn.wolfcode.mq;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wolfcode-lanxw
 */
@Component
@RocketMQMessageListener(consumerGroup ="seckill-OrderTimeOutMQListener",topic = MQConstant.ORDER_PAY_TIMEOUT_TOPIC)
public class OrderTimeOutMQListener implements RocketMQListener<OrderMQResult> {
    @Autowired
    private IOrderInfoService orderInfoService;
    @Override
    public void onMessage(OrderMQResult param) {
        try{
            System.out.println("延迟消息......");
            Long seckillId = param.getSeckillId();
            String orderNo = param.getOrderNo();
            Integer time = param.getTime();
            //判断订单的状态.
            orderInfoService.cancelOrder(time,seckillId,orderNo);
        }catch(BusinessException ex){
            ex.printStackTrace();
            //超时取消订单的时候没有更新到订单状态(可能刚好支付和超时取消碰在一块),通过mq发送消息到某个队列，通知客服人员检查订单状态(人工处理)
        }catch(Exception e){
            e.printStackTrace();
            //超时取消订单的时候没有更新到订单状态(可能刚好支付和超时取消碰在一块),通过mq发送消息到某个队列，通知客服人员检查订单状态(人工处理)
        }
    }
}
