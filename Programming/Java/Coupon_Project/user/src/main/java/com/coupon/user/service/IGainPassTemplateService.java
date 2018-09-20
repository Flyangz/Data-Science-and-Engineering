package com.coupon.user.service;

import com.coupon.user.vo.GainPassTemplateRequest;
import com.coupon.user.vo.Response;

/**
 * <h1>用户领取优惠券功能实现</h1>
 */
public interface IGainPassTemplateService {

    /**
     * <h2>用户领取优惠券</h2>
     * @param request {@link GainPassTemplateRequest}
     * @return {@link Response}
     * @throws Exception
     */
    Response gainPassTemplate(GainPassTemplateRequest request) throws Exception;
}
