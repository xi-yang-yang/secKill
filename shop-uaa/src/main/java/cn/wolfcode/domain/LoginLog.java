package cn.wolfcode.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by lanxw
 */
@Setter@Getter
public class LoginLog implements Serializable {
    public static Boolean LOGIN_SUCCESS = Boolean.TRUE;
    public static Boolean LOGIN_FAIL = Boolean.FALSE;
    public LoginLog(){
        super();
    }
    public LoginLog(Long phone,String loginIp,Date loginTime){
        this.phone = phone;
        this.loginIp = loginIp;
        this.loginTime = loginTime;
    }
    private Long id;
    private Long phone;
    private String loginIp;
    private Date loginTime;
    private Boolean state = LOGIN_SUCCESS;
}
