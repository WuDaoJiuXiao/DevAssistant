package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payload格式类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
@Getter
@AllArgsConstructor
public enum PayloadFormatEnum {
    KEY_VALUE_TO_JSON("键值对 to JSON", "将键值对格式转换为JSON"),
    JSON_TO_KEY_VALUE("JSON to 键值对", "将JSON格式转换为键值对");

    private final String label;
    private final String description;

    @Override
    public String toString() {
        return label;
    }

    public static PayloadFormatEnum findByLabel(String label) {
        for (PayloadFormatEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return KEY_VALUE_TO_JSON;
    }
}