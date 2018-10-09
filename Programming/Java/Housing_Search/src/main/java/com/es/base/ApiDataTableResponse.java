package com.es.base;

import lombok.Getter;
import lombok.Setter;

/**
 * <h1>Data tables响应结果</h1>
 * Created by LiuYang on 2018/10/4 8:37 PM
 */
@Getter
@Setter
public class ApiDataTableResponse extends ApiResponse{

    private int draw;

    // 下面两个字段是Datatables固定格式
    private long recordsTotal;
    private long recordsFiltered;

    public ApiDataTableResponse(ApiResponse.Status status) {
        this(status.getCode(), status.getStandardMessage(), null);
    }

    public ApiDataTableResponse(int code, String message, Object data) {
        super(code, message, data);
    }

}
