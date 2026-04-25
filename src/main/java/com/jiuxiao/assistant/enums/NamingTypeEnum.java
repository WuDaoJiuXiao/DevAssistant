package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 命名类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
@Getter
@AllArgsConstructor
public enum NamingTypeEnum {
    ALL("生成所有命名", "Generate All"),
    CAMEL("驼峰", "camelCase"),
    SNAKE("蛇形", "snake_case"),
    KEBAB("短横线", "kebab-case"),
    PASCAL("帕斯卡", "PascalCase"),
    CONSTANT("常量", "CONSTANT_CASE");

    private final String label;
    private final String description;

    @Override
    public String toString() {
        return label;
    }

    public static NamingTypeEnum findByLabel(String label) {
        for (NamingTypeEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return ALL;
    }
}
