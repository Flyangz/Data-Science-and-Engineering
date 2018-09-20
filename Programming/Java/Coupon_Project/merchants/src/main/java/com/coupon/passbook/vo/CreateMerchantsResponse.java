package com.coupon.passbook.vo;

import com.coupon.passbook.constant.Errorcode;
import com.coupon.passbook.dao.MerchantsDao;
import com.coupon.passbook.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>创建商户响应对象</h1>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMerchantsResponse {

    /**
     * 商户 id：创建失败则为 -1
     */
    private Integer id;

}
