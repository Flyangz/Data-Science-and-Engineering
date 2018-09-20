package com.coupon.user.service;

import com.coupon.user.vo.Pass;
import com.coupon.user.vo.Response;

/**
 * <h1>获取用户个人优惠券信息</h1>
 */
public interface IUserPassService {

    /**
     * <h2>获取用户个人优惠券信息，即我的优惠券功能实现</h2>
     * @param userId
     * @return {@link Response}
     * @throws Exception
     */
    Response getUserPassInfo(Long userId) throws Exception;

    /**
     * <h2>获取用户已经消费了的优惠券，即已使用优惠券功能实现</h2>
     * @param userId
     * @return {@link Response}
     * @throws Exception
     */
    Response getUserUsedPassInfo(Long userId) throws Exception;

    /**
     * <h2>获取用户所有的优惠券</h2>
     * @param userId
     * @return {@link Response}
     * @throws Exception
     */
    Response getUserAllPassInfo(Long userId)throws Exception;

    /**
     * <h2>用户使用优惠券</h2>
     * @param pass {@link Pass}
     * @return {@link Response}
     */
    Response userUsePass(Pass pass);
}
