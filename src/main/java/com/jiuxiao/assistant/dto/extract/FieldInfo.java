package com.jiuxiao.assistant.dto.extract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldInfo {

    /**
     * 字段名称
     */
    private String filedName;

    /**
     * 字段类型
     */
    private TypeInfo fieldType;
}
