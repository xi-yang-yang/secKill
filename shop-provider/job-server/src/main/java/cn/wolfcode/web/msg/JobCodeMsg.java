package cn.wolfcode.web.msg;
import cn.wolfcode.common.web.CodeMsg;

/**
 * Created by wolfcode-lanxw
 */
public class JobCodeMsg extends CodeMsg {
    private JobCodeMsg(Integer code, String msg){
        super(code,msg);
    }
    public static final JobCodeMsg OP_ERROR = new JobCodeMsg(500501,"秒杀服务繁忙");
    public static final JobCodeMsg LOGIN_ERROR = new JobCodeMsg(500102,"账号密码有误");
}
