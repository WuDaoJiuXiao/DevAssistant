package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 解码类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
@Getter
@AllArgsConstructor
public enum DecodeTypeEnum {
    BASE64("Base64解码", "Base64 Decode"),
    URL("URL解码", "URL Decode"),
    HEX("Hex解码", "Hex Decode");

    private final String label;
    private final String description;

    @Override
    public String toString() {
        return label;
    }

    public static DecodeTypeEnum findByLabel(String label) {
        for (DecodeTypeEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return BASE64;
    }
}
