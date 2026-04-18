package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 基本类型枚举类
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
@Getter
@AllArgsConstructor
public enum BasicTypeEnum {

    BYTE("byte"),
    SHORT("short"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    CHAR("char"),
    DEFAULT("default");

    private final String name;

    public static BasicTypeEnum findByName(String simpleName) {
        for (BasicTypeEnum typeEnum : BasicTypeEnum.values()) {
            if (Objects.equals(typeEnum.name, simpleName)) {
                return typeEnum;
            }
        }
        return null;
    }
}
