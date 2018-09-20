package com.coupon.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>用户领取优惠券的请求对象</h1>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GainPassTemplateRequest {

    private Long userId;

    private PassTemplate passTemplate;
}
