package com.coupon.passbook.security;

import com.coupon.passbook.constant.Constants;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限拦截器
 */
@Component
public class AuthCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object o) throws Exception {

        String token = httpServletRequest.getHeader(Constants.TOKEN_STRING);

        // 这里只进行简单校验
        if (StringUtils.isEmpty(token)) {
            throw new Exception("Header 中缺少" + Constants.TOKEN_STRING + "!");
        }

        if (!token.equals(token)) {
            throw new Exception("Header 中 " + Constants.TOKEN_STRING + "错误!");
        }

        AccessContext.setToken(token);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        AccessContext.clearAccessKey();
    }
}
