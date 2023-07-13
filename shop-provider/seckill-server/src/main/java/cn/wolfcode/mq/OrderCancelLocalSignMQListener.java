package cn.wolfcode.mq;

import cn.wolfcode.web.controller.OrderController;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * Created by lanxw
 */
@Component
@RocketMQMessageListener(consumerGroup ="seckill-OrderCancelLocalSignMQListener",topic = MQConstant.CANCEL_SECKILL_OVER_SIGE_TOPIC,messageModel = MessageModel.BROADCASTING)
public class OrderCancelLocalSignMQListener implements RocketMQListener<Long> {
    @Override
    public void onMessage(Long seckillId) {
        OrderController.isStockOverMap.put(seckillId,false);
    }
}
