package com.coupon.user.utils;

import com.coupon.user.vo.Feedback;
import com.coupon.user.vo.GainPassTemplateRequest;
import com.coupon.user.vo.PassTemplate;
import org.apache.commons.codec.digest.DigestUtils;
import org.mortbay.log.Log;

/**
 * <h1>RowKey 生成器工具类</h1>
 */
public class RowKeyGenUtil {

    /**
     * <h2>根据提供的 PassTemplate 对象生成 RowKey</h2>
     *
     * @param passTemplate {@link PassTemplate}
     * @return String RowKey
     */
    public static String genPassTemplateRowKey(PassTemplate passTemplate) {

        // 商户的 id 唯一，而一个商户所发放的优惠券 title 不能相同，所以生成的 RowKey 是唯一的。
        String passInfo = String.valueOf(passTemplate.getId()) + "_" + passTemplate.getTitle();
        String rowKey = DigestUtils.md5Hex(passInfo);
        Log.info("GenPassTemplateRowKey: {}, {}", passInfo, rowKey);

        return rowKey;
    }

    /**
     * <h2>根据提供的领取优惠券请求生成 RowKey，只可以在领取优惠券的时候使用</h2>
     * Pass RowKey = reversed userId + inverse timestamp + PassTemplate RowKey
     * @param request {@link GainPassTemplateRequest}
     * @return String RowKey
     */
    public static String genPassRowKey(GainPassTemplateRequest request){

        return new StringBuilder(String.valueOf(request.getUserId())).reverse().toString()
                + (Long.MAX_VALUE - System.currentTimeMillis())
                + genPassTemplateRowKey(request.getPassTemplate());
    }

    /**
     * <h2>根据 Feedback 构造 RowKey</h2>
     *
     * @param feedback {@link Feedback}
     * @return String RowKey
     */
    public static String genFeedbackRowKey(Feedback feedback) {

        // 对用户 id 进行 reverse 可以使数据更分散。而利用 Long.MAX_VALUE - 创建时间 是为了让最新的反馈排在前面。
        return new StringBuilder(String.valueOf(feedback.getUserId())).reverse().toString() +
                (Long.MAX_VALUE - System.currentTimeMillis());
    }
}
