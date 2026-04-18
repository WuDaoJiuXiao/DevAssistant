package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 属性类型枚举类
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
@Getter
@AllArgsConstructor
public enum FieldTypeEnum {

    BASIC("basic", "基本数据类型"),
    STRING("string", "字符串"),
    CLASS("class", "普通类"),
    INTERFACE("interface", "普通接口"),
    FUNCTIONAL("functional", "函数式接口"),
    ENUM("enum", "枚举"),
    LIST("list", "集合"),
    MAP("map", "键值对"),
    ARRAY("array", "数组"),
    TYPE_PARAM("type_param", "泛型"),
    WILDCARD("wildcard", "通配符"),
    VOID("void", "无返回值"),
    UNKNOWN("unknown", "未知类型");

    private final String name;

    private final String desc;

    public static FieldTypeEnum findByName(String name) {
        for (FieldTypeEnum typeEnum : FieldTypeEnum.values()) {
            if (Objects.equals(typeEnum.name, name)) {
                return typeEnum;
            }
        }
        return null;
    }
}
