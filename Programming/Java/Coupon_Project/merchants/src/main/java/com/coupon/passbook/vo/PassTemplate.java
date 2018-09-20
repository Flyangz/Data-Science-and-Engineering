package com.coupon.passbook.vo;

import com.coupon.passbook.constant.Errorcode;
import com.coupon.passbook.dao.MerchantsDao;
import com.coupon.passbook.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <h1>投放的优惠券对象定义</h1>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassTemplate {

    private Integer id;

    private String title;

    private String summary;

    private String desc;

    private Long limit;

    private Boolean hasToken; //token 存储于 Redis Set 中，每次领取从中获取

    //优惠券背景色
    private Integer background;

    private Date start;

    private Date end;

    /**
     * <h2>校验优惠券对象的有效性</h2>
     *
     * @param merchantsDao {@link MerchantsDao}
     * @return {@link Errorcode}
     */
    public Errorcode validate(MerchantsDao merchantsDao) {

        if (null == merchantsDao.findById(id)) {
            return Errorcode.MERCHANTS_NOT_EXIST;
        }

        return Errorcode.SUCCESS;
    }
}
