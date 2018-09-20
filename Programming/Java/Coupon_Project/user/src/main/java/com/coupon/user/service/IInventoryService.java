package com.coupon.user.service;

import com.coupon.user.vo.Response;

/**
 * <h1>获取库存信息：只返回用户没有领取的，即优惠券库存功能实现接口定义</h1>
 */
public interface IInventoryService {

    /**
     * <h2>获取库存信息</h2>
     * @param userId
     * @return {@link Response}
     * @throws Exception
     */
    Response getInventoryInfo(Long userId) throws Exception;
}
