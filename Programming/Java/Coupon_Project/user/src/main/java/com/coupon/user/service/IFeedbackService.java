package com.coupon.user.service;

import com.coupon.user.vo.Feedback;
import com.coupon.user.vo.Response;

/**
 * <h1>评论功能接口</h1>
 */
public interface IFeedbackService {

    /**
     * <h2>创建评论</h2>
     *
     * @param feedback {@link Feedback}
     * @return {@link Response}
     */
    Response createFeedback(Feedback feedback);

    /**
     * <h2>获取用户评论</h2>
     *
     * @param userId 用户 id
     * @return {@link Response}
     */
    Response getFeedback(Long userId);
}
