package com.coupon.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <h1>投放的优惠券对象定义</h1>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassTemplate {

    /**
     * 发放该优惠券的商户的 id
     */
    private Integer id;

    private String title;

    private String summary;

    private String desc;

    private Long limit;

    private Boolean hasToken;

    private Integer background;

    private Date start;

    private Date end;
}
