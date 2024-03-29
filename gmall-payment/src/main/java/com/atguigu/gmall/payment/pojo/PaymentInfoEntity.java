package com.atguigu.gmall.payment.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/14 18:32
 * @Email: moumouguan@gmail.com
 */
@Data
@TableName("payment_info")
public class PaymentInfoEntity {

    @Id
    private Long id;

    private String outTradeNo;

    private Integer paymentType;

    private String tradeNo;

    private BigDecimal totalAmount;

    private String subject;

    private Integer paymentStatus;

    private Date createTime;

    private Date callbackTime;

    private String callbackContent;
}
