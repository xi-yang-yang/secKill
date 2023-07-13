package cn.wolfcode.mapper;
import cn.wolfcode.domain.AccountTransaction;
import org.apache.ibatis.annotations.Param;

/**
 * Created by lanxw
 */
public interface AccountTransactionMapper {
    void insert(AccountTransaction accountTransaction);
    AccountTransaction get(@Param("txId") String txId, @Param("actionId")String actionId);
    int updateAccountTransactionState(@Param("txId")String txId, @Param("actionId")String actionId, @Param("changeState")int changeState, @Param("checkState")int checkState);
}
