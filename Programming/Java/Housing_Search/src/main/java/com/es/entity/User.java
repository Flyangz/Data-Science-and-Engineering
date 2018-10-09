package com.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by LiuYang on 2018/10/2 5:35 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 要兼容 h2 和 Hibernate ，所以用 IDENTITY 而不是 AUTO
    private Long id;

    private String name;

    private String password;

    private String email;

    @Column(name = "phone_number")
    private String phoneNumber; // MySQL 的列名有下划线，但 java 中不用加，加上 @Column 作为映射即可

    private int status;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_login_time")
    private Date lastLoginTIme;

    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    /** 头像 */
    private String avatar;

    /** 权限信息，SQL表中没有，需要加 @Transient 来避免 jpa 的扫描 */
    @Transient
    private List<GrantedAuthority> authorityList;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorityList;
    }

    @Override
    public String getUsername() {
        return name;
    }

    /** 状态认证，暂时省略 */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 状态认证，暂时省略 */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 状态认证，暂时省略 */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 状态认证，暂时省略 */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
