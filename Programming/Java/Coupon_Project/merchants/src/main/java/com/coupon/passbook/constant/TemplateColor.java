package com.coupon.passbook.constant;

public enum TemplateColor {

    RED(1,"红色"),

    GREEN(2,"绿色"),

    BLUE(3,"蓝色");

    //color
    private Integer code;

    //description
    private String color;

    TemplateColor(Integer code, String color) {
        this.code = code;
        this.color = color;
    }

    public Integer getCode() {
        return code;
    }

    public String getColor() {
        return color;
    }
}
