package com.coupon.user.constant;

/**
 * <h1>优惠券的状态</h1>
 */
public enum PassStatus {

    UNUSED(1, "未被使用"),
    USED(2, "已经使用"),
    ALL(3, "全部领取的");

    private Integer code;

    private String desc;

    PassStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
