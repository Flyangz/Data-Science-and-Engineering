package com.coupon.passbook.service;

import com.coupon.passbook.vo.CreateMerchantsRequest;
import com.coupon.passbook.vo.PassTemplate;
import com.coupon.passbook.vo.Response;

/**
 * <h1>对商户服务接口定义</h1>
 */
public interface IMerchantsServ {

    /**
     * <h2>创建商户服务</h2>
     * @param request {@link CreateMerchantsRequest}
     * @return {@link Response}
     */
    Response createMerchants(CreateMerchantsRequest request);

    /**
     * <h2>根据 id 构造商户信息</h2>
     * @param id 商户 id
     * @return {@link Response}
     */
    Response buildMerchantsInfo(Integer id);

    /**
     * <h2>投放优惠券</h2>
     * @param template {@link PassTemplate}
     * @return {@link Response}
     */
    Response dropPassTemplate(PassTemplate template);
}
