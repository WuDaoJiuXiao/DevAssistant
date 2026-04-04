package com.jiuxiao.assistant.dto.extract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用类型信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeInfo {

    /**
     * 类型分类 {@link com.jiuxiao.assistant.enums.FieldTypeEnum}
     */
    private String category;

    /**
     * 类型名称(简单名)
     */
    private String simpleName;

    /**
     * 类型名称(全限定名)
     */
    private String fullName;

    /**
     * 泛型参数列表
     */
    private List<TypeInfo> genericTypes = new ArrayList<>();

    /**
     * 字段列表
     */
    private List<FieldInfo> fields = new ArrayList<>();

    /**
     * 数组标识
     */
    private boolean isArray = false;
}
