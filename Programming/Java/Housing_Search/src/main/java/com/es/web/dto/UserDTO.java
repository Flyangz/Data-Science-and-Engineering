package com.es.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by LiuYang on 2018/10/5 9:56 PM
 */
@Getter
@Setter
public class UserDTO {

    private Long id;
    private String name;
    private String avatar;
    private String phoneNumber;
    private String lastLoginTime;
}
