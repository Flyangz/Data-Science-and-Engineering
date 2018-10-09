package com.es.security;

import com.es.entity.User;
import com.es.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <h1>自定义认证实现</h1>
 * Created by LiuYang on 2018/10/3 3:05 AM
 */
public class AuthProvider implements AuthenticationProvider {
    @Autowired
    private IUserService userService;

    private final PasswordEncoder passwordEncoder = CustomPasswordEncoderFactory.createDelegatingPasswordEncoder();

    // 已过时，如果后面问题无法解决，可能用回
//    private final Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String userName = authentication.getName();
        final String inputPassword = (String) authentication.getCredentials();

        final User user = userService.findUserByName(userName);
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("authError");
        }

        StringBuilder builder = new StringBuilder();
        final String encodedPassword = builder.append("{MD5}{")
                .append(String.valueOf(user.getId()))
                .append("}")
                .append(user.getPassword())
                .toString();

        // 已过时，如果后面问题无法解决，可能用回
//        if (this.passwordEncoder.isPasswordValid(user.getPassword(), inputPassword, user.getId())) {
        if (this.passwordEncoder.matches(inputPassword, encodedPassword)){
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }

        throw new BadCredentialsException("authError");
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}