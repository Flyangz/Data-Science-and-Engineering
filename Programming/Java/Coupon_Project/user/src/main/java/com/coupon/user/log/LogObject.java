package com.coupon.user.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogObject {

    private String action;

    private Long userId;

    private Long timestamp;

    private String remoteIp;

    private Object info = null;
}
