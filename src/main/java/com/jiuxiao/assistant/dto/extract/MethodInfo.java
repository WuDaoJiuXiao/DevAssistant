package com.jiuxiao.assistant.dto.extract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 方法信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodInfo {

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法参数列表
     */
    private List<MethodParamInfo> parameters = new ArrayList<>();

    /**
     * 返回值类型
     */
    private TypeInfo reType;

    /**
     * 方法修饰符
     */
    private Set<String> modifiers;
}
