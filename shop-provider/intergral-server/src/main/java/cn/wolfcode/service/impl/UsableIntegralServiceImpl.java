package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.domain.AccountLog;
import cn.wolfcode.domain.AccountTransaction;
import cn.wolfcode.domain.OperateIntergralVo;
import cn.wolfcode.domain.UsableIntegral;
import cn.wolfcode.mapper.AccountLogMapper;
import cn.wolfcode.mapper.AccountTransactionMapper;
import cn.wolfcode.mapper.UsableIntegralMapper;
import cn.wolfcode.service.IUsableIntegralService;
import cn.wolfcode.web.msg.IntergralCodeMsg;
import com.alibaba.fastjson.JSON;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by lanxw
 */
@Service
public class UsableIntegralServiceImpl implements IUsableIntegralService {
    @Autowired
    private UsableIntegralMapper usableIntegralMapper;
    @Autowired
    private AccountTransactionMapper accountTransactionMapper;
    @Autowired
    private AccountLogMapper accountLogMapper;
    @Override
    public void decrIntergralTry(OperateIntergralVo operateIntergralVo,BusinessActionContext context) {
        //先插入事务记录，如果成功继续执行，如果失败，说明出现的悬挂，抛出异常.
        insertAccountTransaction(operateIntergralVo, context,AccountTransaction.STATE_TRY);
        //对用户账户进行积分冻结操作
        int count = usableIntegralMapper.freezeIntergral(operateIntergralVo.getUserId(), operateIntergralVo.getValue());
        if(count==0){
            throw new BusinessException(IntergralCodeMsg.INTERGRAL_NOT_ENOUGH);
        }
    }

    @Override
    public void decrIntergralCommit(BusinessActionContext context) {
       AccountTransaction accountTransaction =accountTransactionMapper.get(context.getXid(),String.valueOf(context.getBranchId()));
       //二阶段提交的时候没有这条记录，属于异常的情况
       if(accountTransaction==null){
           //通知管理员，处理问题
           throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
       }
       if(accountTransaction.getState() == AccountTransaction.STATE_TRY){
           OperateIntergralVo operateIntergralVo = JSON.parseObject(context.getActionContext().get("operateIntergralVo").toString(),OperateIntergralVo.class);
           //事务状态处于try,执行commit操作即可
           insertAccountLog(operateIntergralVo,AccountLog.TYPE_DECR);
           int count = accountTransactionMapper.updateAccountTransactionState(accountTransaction.getTxId(),accountTransaction.getActionId(),AccountTransaction.STATE_COMMIT,AccountTransaction.STATE_TRY);
           if(count==0){
               //把事务状态从try变成commit阶段失败,通知管理员，处理问题
               throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
           }
           usableIntegralMapper.commitChange(operateIntergralVo.getUserId(),accountTransaction.getAmount());
       }else if(accountTransaction.getState() == AccountTransaction.STATE_COMMIT){
           //事务状态属于commit,现在继续执行commit,我们保证幂等就可以,不需要做其他逻辑
       }else{
           //事务状态属于cancel,这种属于异常情况,通知管理员，处理问题
           throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
       }
    }
    @Override
    public void decrIntergralRollback(BusinessActionContext context) {
        AccountTransaction accountTransaction =accountTransactionMapper.get(context.getXid(),String.valueOf(context.getBranchId()));
        OperateIntergralVo operateIntergralVo = JSON.parseObject(context.getActionContext().get("operateIntergralVo").toString(),OperateIntergralVo.class);
        if(accountTransaction==null){
            //说明try方法还没有执行，空回滚，需要插入一条事务记录,避免try方法执行.
            insertAccountTransaction(operateIntergralVo, context,AccountTransaction.STATE_CANCEL);
            return;
        }
        if(accountTransaction.getState() == AccountTransaction.STATE_TRY){
            //事务状态处于try,执行cancel操作即可
            int count = accountTransactionMapper.updateAccountTransactionState(accountTransaction.getTxId(),accountTransaction.getActionId(),AccountTransaction.STATE_CANCEL,AccountTransaction.STATE_TRY);
            if(count==0){
                //把事务状态从try变成commit阶段失败,通知管理员，处理问题
                throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
            }
            usableIntegralMapper.unFreezeIntergral(operateIntergralVo.getUserId(),accountTransaction.getAmount());
        }else if(accountTransaction.getState() == AccountTransaction.STATE_COMMIT){
            //事务状态属于commit,这种属于异常情况,通知管理员，处理问题
            throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
        }else{
            //事务状态属于cancel,现在继续执行cancel,我们保证幂等就可以,不需要做其他逻辑
        }
    }

    @Override
    public void incrIntergralTry(OperateIntergralVo operateIntergralVo, BusinessActionContext context) {
        //先插入事务记录，如果成功继续执行，如果失败，说明出现的悬挂，抛出异常.
        insertAccountTransaction(operateIntergralVo, context,AccountTransaction.STATE_TRY);
    }

    @Override
    public void incrIntergralCommit(BusinessActionContext context) {
        AccountTransaction accountTransaction =accountTransactionMapper.get(context.getXid(),String.valueOf(context.getBranchId()));
        //二阶段提交的时候没有这条记录，属于异常的情况
        if(accountTransaction==null){
            //通知管理员，处理问题
            throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
        }
        if(accountTransaction.getState() == AccountTransaction.STATE_TRY){
            OperateIntergralVo operateIntergralVo = JSON.parseObject(context.getActionContext().get("operateIntergralVo").toString(),OperateIntergralVo.class);
            //事务状态处于try,执行commit操作即可
            insertAccountLog(operateIntergralVo,AccountLog.TYPE_INCR);
            int count = accountTransactionMapper.updateAccountTransactionState(accountTransaction.getTxId(),accountTransaction.getActionId(),AccountTransaction.STATE_COMMIT,AccountTransaction.STATE_TRY);
            if(count==0){
                //把事务状态从try变成cancel阶段失败,通知管理员，处理问题
                throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
            }
            usableIntegralMapper.addIntergral(operateIntergralVo.getUserId(),accountTransaction.getAmount());
        }else if(accountTransaction.getState() == AccountTransaction.STATE_COMMIT){
            //事务状态属于commit,现在继续执行commit,我们保证幂等就可以,不需要做其他逻辑
        }else{
            //事务状态属于cancel,这种属于异常情况,通知管理员，处理问题
            throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
        }
    }

    @Override
    public void incrIntergralRollback(BusinessActionContext context) {
        AccountTransaction accountTransaction =accountTransactionMapper.get(context.getXid(),String.valueOf(context.getBranchId()));
        OperateIntergralVo operateIntergralVo = JSON.parseObject(context.getActionContext().get("operateIntergralVo").toString(),OperateIntergralVo.class);
        if(accountTransaction==null){
            //说明try方法还没有执行，空回滚，需要插入一条事务记录,避免try方法执行.
            insertAccountTransaction(operateIntergralVo, context,AccountTransaction.STATE_CANCEL);
            return;
        }
        if(accountTransaction.getState() == AccountTransaction.STATE_TRY){
            //事务状态处于try,执行cancel操作即可
            int count = accountTransactionMapper.updateAccountTransactionState(accountTransaction.getTxId(),accountTransaction.getActionId(),AccountTransaction.STATE_CANCEL,AccountTransaction.STATE_TRY);
            if(count==0){
                //把事务状态从try变成cancel阶段失败,通知管理员，处理问题
                throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
            }
        }else if(accountTransaction.getState() == AccountTransaction.STATE_COMMIT){
            //事务状态属于commit,这种属于异常情况,通知管理员，处理问题
            throw new BusinessException(IntergralCodeMsg.OP_INTERGRAL_ERROR);
        }else{
            //事务状态属于cancel,现在继续执行cancel,我们保证幂等就可以,不需要做其他逻辑
        }
    }

    private void insertAccountLog(OperateIntergralVo operateIntergralVo,int type) {
        AccountLog log = new AccountLog();
        log.setPkValue(operateIntergralVo.getPk());
        log.setAmount(operateIntergralVo.getValue());
        log.setGmtTime(new Date());
        log.setType(type);
        accountLogMapper.insert(log);
    }
    private void insertAccountTransaction(OperateIntergralVo operateIntergralVo, BusinessActionContext context,int state) {
        AccountTransaction accountTransaction = new AccountTransaction();
        accountTransaction.setTxId(context.getXid());
        accountTransaction.setActionId(String.valueOf(context.getBranchId()));
        accountTransaction.setAmount(operateIntergralVo.getValue());
        accountTransaction.setGmtCreated(new Date());
        accountTransaction.setType(operateIntergralVo.getInfo());
        accountTransaction.setUserId(operateIntergralVo.getUserId());
        accountTransaction.setState(state);
        accountTransactionMapper.insert(accountTransaction);
    }

    @Override
    @Transactional
    public void decrIntergral(OperateIntergralVo operateIntergralVo) {
        insertAccountLog(operateIntergralVo,AccountLog.TYPE_DECR);
        //对用户账户进行积分冻结操作
        int count = usableIntegralMapper.freezeIntergral(operateIntergralVo.getUserId(), operateIntergralVo.getValue());
        if(count==0){
            throw new BusinessException(IntergralCodeMsg.INTERGRAL_NOT_ENOUGH);
        }
    }

    @Override
    public void incrIntergral(OperateIntergralVo operateIntergralVo) {
        insertAccountLog(operateIntergralVo,AccountLog.TYPE_INCR);
        usableIntegralMapper.addIntergral(operateIntergralVo.getUserId(),operateIntergralVo.getValue());
    }
}
