package cn.wolfcode.web.feign.callback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OperateIntergralVo;
import cn.wolfcode.web.feign.IntergralFeignApi;
import org.springframework.stereotype.Component;

/**
 * Created by lanxw
 */
@Component
public class IntergralFeignCallback implements IntergralFeignApi {
    @Override
    public Result decrIntergral(OperateIntergralVo operateIntergralVo) {
        return null;
    }

    @Override
    public Result incrIntergral(OperateIntergralVo operateIntergralVo) {
        return null;
    }
}
