package com.jiuxiao.assistant.dto.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestMappingInfo {

    private String url;

    private String requestMethod;

    private String className;

    private String methodName;

    private String qualifiedName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMappingInfo that = (RequestMappingInfo) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(requestMethod, that.requestMethod) &&
                Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, requestMethod, className, methodName);
    }
}
