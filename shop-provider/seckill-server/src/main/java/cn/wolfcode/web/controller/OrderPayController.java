package cn.wolfcode.web.controller;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.web.feign.AlipayPayFeignApi;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/orderPay")
@RefreshScope
public class OrderPayController {
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private AlipayPayFeignApi payFeignApi;
    @Value("${pay.returnUrl}")
    private String returlUrl;
    @Value("${pay.notifyUrl}")
    private String notifyUrl;
    @Value("${pay.frontEndPayUrl}")
    private String frontEndPayUrl;
    @RequestMapping("/alipay")
    public Result<String> alipay(String orderNo,int type) throws IOException {
        if(type==OrderInfo.PAYTYPE_ONLINE){
            return alipayOnline(orderNo);
        }else{
            orderInfoService.payByIntergral(orderNo,type);
            return Result.success("");
        }
    }
    private Result alipayOnline(String orderNo){
        OrderInfo orderInfo = orderInfoService.find(orderNo);
        if(orderInfo==null){
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        PayVo payVo = new PayVo();
        payVo.setOutTradeNo(orderInfo.getOrderNo());
        payVo.setTotalAmount(String.valueOf(orderInfo.getSeckillPrice()));
        payVo.setSubject(orderInfo.getProductName());
        payVo.setBody(orderInfo.getProductName());
        payVo.setReturnUrl(returlUrl);
        payVo.setNotifyUrl(notifyUrl);
        Result<String> result = payFeignApi.pay(payVo);
        if(result==null || result.hasError()){
            return Result.error(SeckillCodeMsg.PAY_SERVER_ERROR);
        }
        //输出
        return result;
    }

    @RequestMapping("/refund")
    public Result<String> refund(String orderNo) throws IOException {
        OrderInfo orderInfo = orderInfoService.find(orderNo);
        if(orderInfo==null){
            throw new BusinessException(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        if(orderInfo.getPayType()==0){
            orderInfoService.refund(orderInfo);
        }else{
            orderInfoService.refundByIntergral(orderInfo);
        }
        return Result.success("");
    }
    @RequestMapping("/notify_url")
    @ResponseBody
    public String notifyUrl(@RequestParam Map<String,String> params)  {
        System.out.println("异步回调====>"+new Date());
        Result<Boolean> result = payFeignApi.signCheck(params);
        if(result==null || result.hasError() || result.hasError()){
            return "fail";
        }
        //在异步回调的时候，业务做了，但是在返回"success"的时候，支付宝没有收到响应请求.继续在发送请求.此方法有可能会被多次调用
        //做幂等性判断，会使用支付流水表来控制，防止业务重复执行.
        int count  = orderInfoService.paySuccess(params);
        if(count==0){
            //在支付修改订单状态的时候，刚好已经超时取消订单了，订单状态是不能修改的。进行退款流程.
            //往MQ中发送消息，做退款操作，人工需要审核之后才进行退款操作.
        }
        //修改订单状态
        return "success";

    }
    @RequestMapping("/return_url")
    public void returnUrl(@RequestParam Map<String,String> params, HttpServletResponse response) throws IOException {
        System.out.println("同步回调====>"+new Date());
        Result<Boolean> result =  payFeignApi.signCheck(params);
        if(result==null || result.hasError() || result.hasError()){
            response.sendRedirect("http://localhost/50x.html");
            return;
        }
        String out_trade_no = params.get("out_trade_no");
        response.sendRedirect(frontEndPayUrl+out_trade_no);
    }
}
