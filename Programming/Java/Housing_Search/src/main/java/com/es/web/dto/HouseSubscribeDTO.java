package com.es.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by LiuYang on 2018/10/8 1:25 PM
 */
@Getter
@Setter
public class HouseSubscribeDTO {
    private Long id;

    private Long houseId;

    private Long userId;

    private Long adminId;

    // 预约状态 1-加入待看清单 2-已预约看房时间 3-看房完成
    private int status;

    private Date createTime;

    private Date lastUpdateTime;

    private Date orderTime;

    private String telephone;

    private String desc;
}
