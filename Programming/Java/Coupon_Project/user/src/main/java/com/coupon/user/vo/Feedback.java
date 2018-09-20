package com.coupon.user.vo;

import com.coupon.user.constant.FeedbackType;
import com.google.common.base.Enums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>用户评论</h1>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    private Long userId;

    private String type;

    private String templateId;

    private String comment;

    public boolean validate() {

        FeedbackType feedbackType = Enums.getIfPresent(
                FeedbackType.class, this.type.toUpperCase()
        ).orNull();

        return !(null == feedbackType || null == comment);
    }
}
