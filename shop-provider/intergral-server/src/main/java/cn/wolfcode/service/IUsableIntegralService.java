package cn.wolfcode.service;

import cn.wolfcode.domain.OperateIntergralVo;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * Created by lanxw
 */
@LocalTCC
public interface IUsableIntegralService {
    /**
     * 减少积分
     * @param operateIntergralVo
     * @return 变化后的用户积分
     */
    @TwoPhaseBusinessAction(name = "decrIntergralTry", commitMethod = "decrIntergralCommit", rollbackMethod = "decrIntergralRollback")
    void decrIntergralTry(@BusinessActionContextParameter(paramName = "operateIntergralVo") OperateIntergralVo operateIntergralVo,BusinessActionContext context);
    void decrIntergralCommit(BusinessActionContext context);
    void decrIntergralRollback(BusinessActionContext context);
    @TwoPhaseBusinessAction(name = "incrIntergralTry", commitMethod = "incrIntergralCommit", rollbackMethod = "incrIntergralRollback")
    void incrIntergralTry(@BusinessActionContextParameter(paramName = "operateIntergralVo")OperateIntergralVo operateIntergralVo,BusinessActionContext context);
    void incrIntergralCommit(BusinessActionContext context);
    void incrIntergralRollback(BusinessActionContext context);

    /**
     * 减少积分
     * @param operateIntergralVo
     * @return 变化后的用户积分
     */
    void decrIntergral(OperateIntergralVo operateIntergralVo);
    /**
     * 增加积分
     * @param operateIntergralVo
     * @return 变化后的用户积分
     */
    void incrIntergral(OperateIntergralVo operateIntergralVo);
}
