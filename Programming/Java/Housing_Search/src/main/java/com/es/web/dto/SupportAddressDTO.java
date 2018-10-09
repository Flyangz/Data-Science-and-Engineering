package com.es.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <h1>输出到前端的类</h1>
 * Created by LiuYang on 2018/10/3 11:19 PM
 */
@Getter
@Setter
@ToString
public class SupportAddressDTO {

    private Long id;

    @JsonProperty(value = "belong_to")
    private String belongTo;

    @JsonProperty(value = "en_name")
    private String enName;

    @JsonProperty(value = "cn_name")
    private String cnName;

    private String level;

}
