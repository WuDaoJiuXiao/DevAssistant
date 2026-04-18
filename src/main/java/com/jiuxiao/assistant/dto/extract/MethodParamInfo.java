package com.jiuxiao.assistant.dto.extract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方法参数信息实体类
 *
 * @author 悟道九霄
 * @date 2026-04-18
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
