package cn.wolfcode.web.msg;
import cn.wolfcode.common.web.CodeMsg;

/**
 * Created by wolfcode-lanxw
 */
public class PayCodeMsg extends CodeMsg {
    private PayCodeMsg(Integer code, String msg){
        super(code,msg);
    }
}
