package cn.wolfcode.mapper;

import cn.wolfcode.domain.UsableIntegral;
import org.apache.ibatis.annotations.Param;

/**
 * Created by lanxw
 */
public interface UsableIntegralMapper {
    int freezeIntergral(@Param("userId") Long userId, @Param("amount")Long amount);
    int commitChange(@Param("userId")Long userId, @Param("amount")Long amount);
    void unFreezeIntergral(@Param("userId")Long userId, @Param("amount")Long amount);
    void addIntergral(@Param("userId")Long userId, @Param("amount")Long amount);
}
