package com.coupon.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    /**
     * 用户基本信息
     */
    private BaseInfo baseInfo;

    /**
     * 用户额外信息
     */
    private OtherInfo otherInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseInfo {

        private String name;
        private Integer age;
        private String sex;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherInfo {

        private String phone;
        private String address;
    }
}
