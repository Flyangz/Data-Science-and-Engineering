package com.es.web.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <h1>Datatables表单</h1>
 * Created by LiuYang on 2018/10/4 8:40 PM
 */
@Getter
@Setter
public class DatatableSearch {
    /**
     * <h2>Datatables要求回显字段</h2>
     */
    private int draw;

    /**
     * <h2>Datatables规定分页字段名</h2>
     */
    private int start;

    /**
     * <h2>Datatables规定分页字段名
     * 前端控制，一页显示多少条数据</h2>
     */
    private int length;

    /**
     * <h2>MySQL表中是否已审核状态码</h2>
     */
    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTimeMin;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTimeMax;

    private String city;
    private String title;
    private String direction;
    private String orderBy;
}
