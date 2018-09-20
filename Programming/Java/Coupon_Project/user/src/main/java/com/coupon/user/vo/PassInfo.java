package com.coupon.user.vo;

import com.coupon.user.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>用户领取的优惠券信息</h1>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassInfo {

    private Pass pass;

    private PassTemplate passTemplate;

    private Merchants merchants;
}
