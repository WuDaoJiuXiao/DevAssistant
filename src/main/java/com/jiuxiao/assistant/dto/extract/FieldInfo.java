package com.jiuxiao.assistant.dto.extract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段信息实体类
 *
 * @author 悟道九霄
 * @date 2026-04-18
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
