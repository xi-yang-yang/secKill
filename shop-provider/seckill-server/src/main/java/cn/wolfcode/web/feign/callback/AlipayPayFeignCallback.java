package cn.wolfcode.web.feign.callback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import cn.wolfcode.web.feign.AlipayPayFeignApi;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by lanxw
 */
@Component
public class AlipayPayFeignCallback implements AlipayPayFeignApi {
    @Override
    public Result<String> pay(PayVo payVo) {
        return null;
    }

    @Override
    public Result<Boolean> signCheck(Map<String, String> param) {
        return null;
    }

    @Override
    public Result<Boolean> refund(RefundVo refundVo) {
        return null;
    }
}
