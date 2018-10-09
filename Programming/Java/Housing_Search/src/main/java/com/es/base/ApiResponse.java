package com.es.base;

import lombok.Getter;
import lombok.Setter;

/**
 * <h1>API 格式封装</h1>
 * Created by LiuYang on 2018/10/2 9:56 PM
 */
@Getter
@Setter
public class ApiResponse {
    private int code;
    private String message;
    private Object data;

    /** 表示数据集是否还有更多的信息 */
    private boolean more;

    // 开发时用来确定某功能是否能用，完成后的代码不会使用此方法
    public ApiResponse(int code) {
        this.code = Status.SUCCESS.getCode();
        this.message = Status.SUCCESS.getStandardMessage();
    }

    // 完全自定义构造器，下面的3种特殊构造函数要用到。
    public ApiResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 返回非常规信息，如自定义的错误码和信息
    // 使用时参数例子：ApiResponse.Status.XXX.getCode(), serviceResult.getMessage()
    public static ApiResponse ofMessage(int code, String message){
        return new ApiResponse(code, message, null);
    }

    // 成功并返回对象给前端
    // 使用时参数例子：serviceResult.getResult()
    public static ApiResponse ofSuccess(Object data) {
        return new ApiResponse(Status.SUCCESS.getCode(), Status.SUCCESS.getStandardMessage(), data);
    }

    // 常规返回，即通用的错误码、错误信息、没有返回对象
    // 使用时参数例子：ApiResponse.Status.XXX
    public static ApiResponse ofStatus(Status status) {
        return new ApiResponse(status.getCode(), status.getStandardMessage(), null);
    }

    public enum Status {
        SUCCESS(200, "OK"),
        BAD_REQUEST(400, "Bad Request"),
        NOT_FOUND(404, "Not Found"),
        INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"),
        NOT_VALID_PARAM(40005, "Not valid Params"),
        NOT_SUPPORTED_OPERATION(40006, "Operation not supported"),
        NOT_LOGIN(50000, "Not Login");

        private int code;
        private String standardMessage;

        Status(int code, String standardMessage) {
            this.code = code;
            this.standardMessage = standardMessage;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getStandardMessage() {
            return standardMessage;
        }

        public void setStandardMessage(String standardMessage) {
            this.standardMessage = standardMessage;
        }
    }
}
