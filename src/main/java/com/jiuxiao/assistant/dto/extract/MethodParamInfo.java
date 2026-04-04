package com.jiuxiao.assistant.dto.extract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方法参数信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodParamInfo {

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数类型
     */
    private TypeInfo paramType;
}
