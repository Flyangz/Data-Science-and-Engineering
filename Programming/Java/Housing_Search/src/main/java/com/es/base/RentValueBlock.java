package com.es.base;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * <h1>带区间的常用数值定义</h1>
 * 正常来说会存在数据库，这里简单地存到内存
 * Created by LiuYang on 2018/10/5 4:49 PM
 */
@Getter
@Setter
public class RentValueBlock {

    private String key;
    private int min;
    private int max;

    public RentValueBlock(String key, int min, int max) {
        this.key = key;
        this.min = min;
        this.max = max;
    }

    /**
     * 价格区间定义
     */
    public static final Map<String, RentValueBlock> PRICE_BLOCK;

    /**
     * 面积区间定义
     */
    public static final Map<String, RentValueBlock> AREA_BLOCK;

    /**
     * <h2>初始化默认的区间</h2>
     */
    static {
        PRICE_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-1000", new RentValueBlock("*-1000", -1, 1000))
                .put("1000-3000", new RentValueBlock("1000-3000", 1000, 3000))
                .put("3000-*", new RentValueBlock("3000-*", 3000, -1))
                .build();

        AREA_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-30", new RentValueBlock("*-30", -1, 30))
                .put("30-50", new RentValueBlock("30-50", 30, 50))
                .put("50-*", new RentValueBlock("50-*", 50, -1))
                .build();
    }

    /**
     * 无限制区间
     */
    public static final RentValueBlock ALL = new RentValueBlock("*", -1, -1);

    /** 进行匹配，如果没有这种分类，就返回无限制区间 */
    public static RentValueBlock matchPrice(String key) {
        RentValueBlock block = PRICE_BLOCK.get(key);
        if (block == null) {
            return ALL;
        }
        return block;
    }

    /** 进行匹配，如果没有这种分类，就返回无限制区间 */
    public static RentValueBlock matchArea(String key) {
        RentValueBlock block = AREA_BLOCK.get(key);
        if (block == null) {
            return ALL;
        }
        return block;
    }
}