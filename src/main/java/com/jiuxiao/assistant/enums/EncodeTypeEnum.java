package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 编码类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
@Getter
@AllArgsConstructor
public enum EncodeTypeEnum {
    BASE64("Base64编码", "Base64 Encode"),
    URL("URL编码", "URL Encode"),
    HEX("Hex编码", "Hex Encode"),
    MD5("MD5摘要", "MD5 Hash"),
    SHA256("SHA-256摘要", "SHA-256 Hash"),
    SHA512("SHA-512摘要", "SHA-512 Hash");

    private final String label;
    private final String description;

    @Override
    public String toString() {
        return label;
    }

    public static EncodeTypeEnum findByLabel(String label) {
        for (EncodeTypeEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return BASE64;
    }
}
