package com.jiuxiao.assistant.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 请求方式枚举类
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
@Getter
public enum RequestMethodEnum {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH");

    private final String method;

    RequestMethodEnum(String method) {
        this.method = method;
    }

    public static RequestMethodEnum findByMethod(String method) {
        for (RequestMethodEnum methodEnum : RequestMethodEnum.values()) {
            if (Objects.equals(methodEnum.method, method)) {
                return methodEnum;
            }
        }
        return null;
    }
}
