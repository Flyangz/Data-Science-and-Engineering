package com.coupon.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <h1>库存请求响应</h1>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long userId;

    private List<PassTemplateInfo> passTemplateInfo;
}
