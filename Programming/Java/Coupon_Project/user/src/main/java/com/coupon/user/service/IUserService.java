package com.coupon.user.service;

import com.coupon.user.vo.Response;
import com.coupon.user.vo.User;

/**
 * <h1>用户服务：创建 User 服务</h1>
 */
public interface IUserService {

    /**
     * <h2>创建用户</h2>
     *
     * @param user {@link User}
     * @return {@link Response}
     * @throws Exception
     */
    Response createUser(User user) throws Exception;
}
