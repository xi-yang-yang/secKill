package cn.wolfcode.mapper;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.domain.LoginLog;
import cn.wolfcode.domain.UserLogin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by wolfcode-lanxw
 */
public interface UserMapper {
    UserLogin selectUserLoginByPhone(Long phone);
    UserInfo selectUserInfoByPhone(Long phone);
    int insertLoginLong(LoginLog loginLog);
}
