package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 去空格类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
@Getter
@AllArgsConstructor
public enum TrimSpaceTypeEnum {

    REMOVE_ALL("去除所有空格", "\\s+", ""),
    REMOVE_NEWLINE("去除换行符", "[\n\r]+", ""),
    TRIM_ONLY("首尾去空格", "", "trim"),
    REMOVE_ALL_AND_TRIM("去除所有+首尾", "\\s+", "trim");

    private final String label;
    private final String spaceRegex;
    private final String trimType;

    @Override
    public String toString() {
        return label;
    }

    public static TrimSpaceTypeEnum findByLabel(String label) {
        for (TrimSpaceTypeEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return REMOVE_ALL;
    }
}