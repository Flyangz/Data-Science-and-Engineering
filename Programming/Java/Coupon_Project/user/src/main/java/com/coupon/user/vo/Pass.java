package com.coupon.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <h2>用户领取的优惠券</h2>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pass {

    private Long userId;

    /**
     * pass 在 HBase 中的 RowKey
     */
    private String rowKey;

    /**
     * PassTemplate 在 HBase 中的 RowKey
     */
    private String templateId;

    /**
     * 优惠券 token，有可能为 null，则填充为 -1
     */
    private String token;

    /**
     * 领取日期
     */
    private Date assignedDate;

    /**
     * 消费日期，非空代表已被消费
     */
    private Date conDate;
}
