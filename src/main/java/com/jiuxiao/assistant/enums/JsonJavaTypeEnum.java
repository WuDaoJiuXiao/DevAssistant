package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JSON与Java转换类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
@Getter
@AllArgsConstructor
public enum JsonJavaTypeEnum {
    JSON_TO_JAVA("JSON to Java", "JSON转换为Java类"),
    JAVA_TO_JSON("Java to JSON", "Java类转换为JSON");

    private final String label;
    private final String description;

    @Override
    public String toString() {
        return label;
    }

    public static JsonJavaTypeEnum findByLabel(String label) {
        for (JsonJavaTypeEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return JSON_TO_JAVA;
    }
}