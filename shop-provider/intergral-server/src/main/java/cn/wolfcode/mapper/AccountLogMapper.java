package cn.wolfcode.mapper;
import cn.wolfcode.domain.AccountLog;
import cn.wolfcode.domain.AccountTransaction;
import org.apache.ibatis.annotations.Param;

/**
 * Created by lanxw
 */
public interface AccountLogMapper {
    void insert(AccountLog accountLog);
}
