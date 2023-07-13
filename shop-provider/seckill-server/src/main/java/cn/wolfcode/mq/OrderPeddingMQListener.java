package cn.wolfcode.mq;

import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
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
@RocketMQMessageListener(consumerGroup ="seckill-OrderPeddingMQListener",topic = MQConstant.ORDER_PEDDING_TOPIC)
public class OrderPeddingMQListener implements RocketMQListener<OrderMessage> {
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Override
    public void onMessage(OrderMessage orderMessage) {
        OrderMQResult param = new OrderMQResult();
        param.setTime(orderMessage.getTime());
        param.setSeckillId(orderMessage.getSeckillId());
        String tag;
        try{
            String orderNo = orderInfoService.doSeckill(orderMessage.getTime(), orderMessage.getSeckillId(),orderMessage.getUserPhone());
            param.setOrderNo(orderNo);
            param.setMsg("下单成功，请尽快支付.");
            tag = MQConstant.ORDER_RESULT_SUCCESS_TAG;
            //发送延时消息
            org.springframework.messaging.Message<OrderMQResult> msg = MessageBuilder.withPayload(param).build();
            rocketMQTemplate.syncSend(MQConstant.ORDER_PAY_TIMEOUT_TOPIC, MessageBuilder.withPayload(param).build(),3000,MQConstant.ORDER_PAY_TIMEOUT_DELAY_LEVEL);
        }catch(Exception e){
            e.printStackTrace();
            param.setCode(SeckillCodeMsg.SECKILL_ERROR.getCode());
            param.setMsg(SeckillCodeMsg.SECKILL_ERROR.getMsg());
            tag = MQConstant.ORDER_RESULT_FAIL_TAG;
        }
        //发送通知消息
        param.setToken(orderMessage.getToken());
        rocketMQTemplate.syncSend(MQConstant.ORDER_RESULT_TOPIC+":"+tag,param);
    }
}
