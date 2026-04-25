package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 进制类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
@Getter
@AllArgsConstructor
public enum NumBaseEnum {
    BINARY(2, "二进制", "0b"),
    OCTAL(8, "八进制", "0o"),
    DECIMAL(10, "十进制", ""),
    HEX(16, "十六进制", "0x"),
    BASE36(36, "三十六进制", "");

    private final int value;
    private final String label;
    private final String prefix;

    public static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static NumBaseEnum findByValue(int value) {
        for (NumBaseEnum numBaseEnum : values()) {
            if (numBaseEnum.value == value) {
                return numBaseEnum;
            }
        }
        return DECIMAL;
    }

    @Override
    public String toString() {
        return value + " (" + label + ")";
    }
}
