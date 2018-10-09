package com.es.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

/**
 * <h1>文件上传配置</h1>
 * Created by LiuYang on 2018/10/3 4:50 PM
 */
@Configuration
@ConditionalOnClass({Servlet.class, StandardServletMultipartResolver.class,
        MultipartConfigElement.class})

// 判断某个配置是否生效，此处为以 spring.http.multipart 作前缀的。
// 这里会去配置文件中找 spring.http.multipart.enabled 的值与 havingValue（默认为""）进行比较。
// 如果没有找到这个配置，由 matchIfMissing 返回需要和 havingValue 比较的值。
@ConditionalOnProperty(prefix = "spring.http.multipart", name = "enabled",
    matchIfMissing = true)
@EnableConfigurationProperties(MultipartProperties.class) // 允许 spring 自动配置
public class WebFileUploadConfig {

    private final MultipartProperties multipartProperties;

    public WebFileUploadConfig(MultipartProperties multipartProperties) {
        this.multipartProperties = multipartProperties;
    }

    /** 上传配置 */
    @Bean
    @ConditionalOnMissingBean // 如果 container 中已经有 MultipartConfigElement 就不会执行这个工厂方法
    public MultipartConfigElement multipartConfigElement(){
        return this.multipartProperties.createMultipartConfig();
    }

    /** 注册解析器 */
    // 暂略
    @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    @ConditionalOnMissingBean(MultipartResolver.class)
    public StandardServletMultipartResolver multipartResolver() {
        StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
        multipartResolver.setResolveLazily(this.multipartProperties.isResolveLazily());
        return multipartResolver;
    }
}
