package com.es.base;

/**
 * <h1>房屋操作状态常量定义</h1>
 * Created by LiuYang on 2018/10/5 3:37 PM
 */
public class HouseOperation {

    /** 通过审核 */
    public static final int PASS = 1;

    /** 下架。重新审核 */
    public static final int PULL_OUT = 2;

    /** 逻辑删除 */
    public static final int DELETE = 3;

    /** 出租 */
    public static final int RENT = 4;
}
