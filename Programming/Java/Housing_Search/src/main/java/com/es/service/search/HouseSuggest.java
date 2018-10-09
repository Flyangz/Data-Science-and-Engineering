package com.es.service.search;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by LiuYang on 2018/10/7 6:17 PM
 */
@Getter
@Setter
public class HouseSuggest {
    private String input;
    private int weight = 10; // 默认权重
}
