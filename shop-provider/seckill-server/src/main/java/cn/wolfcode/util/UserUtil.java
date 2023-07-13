package cn.wolfcode.util;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.redis.CommonRedisKey;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by lanxw
 */
public class UserUtil {
    public static UserInfo getUser(StringRedisTemplate redisTemplate, String token){

        String strObj = redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token));
        UserInfo userInfo = JSON.parseObject(strObj,UserInfo.class);
        return userInfo;
    }
}
