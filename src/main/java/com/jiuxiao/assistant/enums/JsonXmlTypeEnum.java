package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JSON与XML转换类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
@Getter
@AllArgsConstructor
public enum JsonXmlTypeEnum {
    JSON_TO_XML("JSON to XML", "JSON转换为XML"),
    XML_TO_JSON("XML to JSON", "XML转换为JSON");

    private final String label;
    private final String description;

    @Override
    public String toString() {
        return label;
    }

    public static JsonXmlTypeEnum findByLabel(String label) {
        for (JsonXmlTypeEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return JSON_TO_XML;
    }
}