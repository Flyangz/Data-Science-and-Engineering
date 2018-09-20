package com.coupon.passbook.vo;

import com.coupon.passbook.constant.Errorcode;
import com.coupon.passbook.dao.MerchantsDao;
import com.coupon.passbook.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>创建商户请求对象</h1>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMerchantsRequest {

    private String name;

    private String logoUrl;

    private String businessLicenseUrl;

    private String phone;

    private String address;

    /**
     * <h2>检验请求的有效性</h2>
     *
     * @param merchantsDao {@link MerchantsDao}
     * @return {@link Errorcode}
     */
    public Errorcode validate(MerchantsDao merchantsDao) {

        if (merchantsDao.findByName(name) != null) {
            return Errorcode.DUPLICATE_NAME;

        } else if (logoUrl == null) {
            return Errorcode.EMPTY_LOGO;

        } else if (businessLicenseUrl == null) {
            return Errorcode.EMPTY_BUSINESS_LICENSE;

        } else if (address == null) {
            return Errorcode.EMPTY_ADDRESS;

        } else if (phone == null) {
            return Errorcode.ERROR_PHONE;

        } else {
            return Errorcode.SUCCESS;
        }
    }

    /**
     * <h2>将请求对象转换为可以保存到数据库的CreateMerchantsRequest商户对象</h2>
     *
     * @return {@link Merchants}
     */
    public Merchants toMerchants() {

        Merchants merchants = new Merchants();

        merchants.setName(name);
        merchants.setLogoUrl(logoUrl);
        merchants.setBusinessLicenseUrl(businessLicenseUrl);
        merchants.setPhone(phone);
        merchants.setAddress(address);

        return merchants;
    }
}
