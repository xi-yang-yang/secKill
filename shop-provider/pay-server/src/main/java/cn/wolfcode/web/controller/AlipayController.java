package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.config.AlipayProperties;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/alipay")
public class AlipayController {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayProperties alipayProperties;
    /**
     * 支付接口，调用之后返回html的内容,返回给前端后，前端需要添加到页面中，执行才会跳转到支付宝页面
     * @param payVo
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("/pay")
    Result<String> alipay(@RequestBody PayVo payVo) throws AlipayApiException {
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(payVo.getReturnUrl());//同步回调
        alipayRequest.setNotifyUrl(payVo.getNotifyUrl());//异步回调
        //拼装请求参数
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ payVo.getOutTradeNo() +"\","
                + "\"total_amount\":\""+ payVo.getTotalAmount() +"\","
                + "\"subject\":\""+ payVo.getSubject() +"\","
                + "\"body\":\""+ payVo.getBody() +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        return Result.success(result);
    }

    /**
     *  对回调的参数进行验签操作
     * @param param 支付宝回调的参数
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("/signCheck")
    Result<Boolean> signCheckByAlipay(@RequestParam Map<String,String> param) throws AlipayApiException {
        boolean signVerified = AlipaySignature.rsaCheckV1(param, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType()); //调用SDK验证签名
        return Result.success(signVerified);
    }

    /**
     * 退款接口
     * @param refundVo
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("/refund")
    Result<Boolean> refundByAlipay(@RequestBody RefundVo refundVo) throws AlipayApiException {
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ refundVo.getOutTradeNo() +"\","
                + "\"trade_no\":\"\","
                + "\"refund_amount\":\""+ refundVo.getRefundAmount() +"\","
                + "\"refund_reason\":\""+ refundVo.getRefundReason() +"\","
                + "\"out_request_no\":\""+ refundVo.getOutTradeNo() +"\"}");
        AlipayTradeRefundResponse execute = alipayClient.execute(alipayRequest);
        return Result.success(execute.isSuccess());
    }

}
