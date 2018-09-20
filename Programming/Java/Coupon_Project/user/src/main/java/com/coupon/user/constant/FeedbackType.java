package com.coupon.user.constant;

/**
 * <h1>评论类型</h1>
 */
public enum FeedbackType {

    PASS("pass", "针对优惠券的评论"),
    APP("app", "针对卡包 APP 的评论");

    private String code;

    private String desc;

    FeedbackType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
