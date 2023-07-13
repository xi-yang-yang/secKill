package cn.wolfcode.mapper;

import cn.wolfcode.domain.OrderInfo;
import org.apache.ibatis.annotations.*;

/**
 * Created by wolfcode-lanxw
 */
public interface OrderInfoMapper {
    int insert(OrderInfo orderInfo);
    OrderInfo find(String orderNo);
    int updateCancelStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);
    int changePayStatus(@Param("orderNo") String orderNo, @Param("status") Integer status,@Param("payType")int payType);
    int changeRefundStatus(@Param("orderNo")String outTradeNo, @Param("status")Integer statusRefund);
}
