package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import cn.wolfcode.web.feign.callback.AlipayPayFeignCallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by lanxw
 */
@FeignClient(name = "pay-service",fallback = AlipayPayFeignCallback.class)
public interface AlipayPayFeignApi {
    @RequestMapping("/alipay/pay")
    Result<String> pay(@RequestBody PayVo payVo);
    @RequestMapping("/alipay/signCheck")
    Result<Boolean> signCheck(@RequestParam Map<String,String> param);
    @RequestMapping("/alipay/refund")
    Result<Boolean> refund(@RequestBody RefundVo refundVo);
}
