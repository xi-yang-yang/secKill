package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OperateIntergralVo;
import cn.wolfcode.web.feign.callback.IntergralFeignCallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by lanxw
 */
@FeignClient(name = "intergral-service",fallback = IntergralFeignCallback.class)
public interface IntergralFeignApi {
    @RequestMapping("/intergral/decrIntergral")
    Result decrIntergral(@RequestBody OperateIntergralVo operateIntergralVo);
    @RequestMapping("/intergral/incrIntergral")
    Result incrIntergral(@RequestBody OperateIntergralVo operateIntergralVo);
}
