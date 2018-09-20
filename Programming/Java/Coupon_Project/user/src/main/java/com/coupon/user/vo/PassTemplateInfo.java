package com.coupon.user.vo;

import com.coupon.user.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <h1>优惠券模版信息</h1>
 */
//@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassTemplateInfo {

    /** 优惠券模版 */
    private PassTemplate passTemplate;

    /** 优惠券对应的商户 */
    private Merchants merchants;
}
