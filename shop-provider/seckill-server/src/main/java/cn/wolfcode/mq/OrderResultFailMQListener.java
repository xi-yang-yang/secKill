package cn.wolfcode.mq;

import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
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
@RocketMQMessageListener(consumerGroup ="seckill-OrderResultFailMQListener",topic = MQConstant.ORDER_RESULT_TOPIC,selectorExpression = MQConstant.ORDER_RESULT_FAIL_TAG)
public class OrderResultFailMQListener implements RocketMQListener<OrderMQResult> {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Override
    public void onMessage(OrderMQResult param) {
        try{
            System.out.println("库存回补");
            Long seckillId = param.getSeckillId();
            Integer time = param.getTime();
            seckillProductService.syncRedisStockCount(time,seckillId);
        }catch(Exception e){
            e.printStackTrace();
            //同步预库存失败，发送短信/邮件，人工处理.
        }
    }
}
