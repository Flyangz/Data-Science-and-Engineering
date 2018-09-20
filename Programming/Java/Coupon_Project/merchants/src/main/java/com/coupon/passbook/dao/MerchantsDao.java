package com.coupon.passbook.dao;

import com.coupon.passbook.entity.Merchants;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.naming.Name;

/**
 * spring boot 的 jpa 让我们可以通过实现接口，来简便地实现 sql 查询。
 */
public interface MerchantsDao extends JpaRepository<Merchants, Integer> {

    /**
     * <h2>根据 id 获取商户对象</h2>
     *
     * @param id 商户 id
     * @return {@link Merchants}
     */
    Merchants findById(Integer id);

    /**
     * <h2>根据商户名称获取商户对象</h2>
     *
     * @param name 商户名称
     * @return {@link Merchants}
     */
    Merchants findByName(String name);

}
