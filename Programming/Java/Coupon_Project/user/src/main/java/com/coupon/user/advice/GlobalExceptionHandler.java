package com.coupon.user.advice;

import com.coupon.user.vo.ErrorInfo;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * <h1>全局异常处理</h1>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ErrorInfo<String> errorHandler(HttpServletRequest request, Exception e) throws Exception {

        ErrorInfo<String> info = new ErrorInfo<String>();

        info.setCode(ErrorInfo.ERROR);
        info.setMessage(e.getMessage());
        info.setData("Do not have returned data");
        info.setUrl(request.getRequestURL().toString());

        return info;
    }
}
