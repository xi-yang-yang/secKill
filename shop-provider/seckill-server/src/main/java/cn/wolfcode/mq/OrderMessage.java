package cn.wolfcode.mq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by wolfcode-lanxw
 * 封装异步下单的参数
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {
    private Integer time;
    private Long seckillId;
    private String token;
    private Long userPhone;
}
