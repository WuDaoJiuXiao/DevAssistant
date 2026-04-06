package com.jiuxiao.assistant.dto.mock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Controller请求信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {

    /**
     * 请求URL
     */
    private String url;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求参数对象(转换后的JSON格式)
     */
    private Object requestBody;
}
