package cn.wolfcode.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by lanxw
 */
@Setter@Getter
public class UserLogin implements Serializable {
    private Long phone;
    private String password;
    private String salt;
}
