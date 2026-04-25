package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据生成类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
@Getter
@AllArgsConstructor
public enum DataGenTypeEnum {
    UUID("UUID", "UUID"),
    SNOWFLAKE("雪花ID", "Snowflake ID"),
    TIMESTAMP_MS("时间戳(毫秒)", "Timestamp (ms)"),
    TIMESTAMP_SEC("时间戳(秒)", "Timestamp (s)"),
    RANDOM_INT("随机整数", "Random Integer"),
    RANDOM_STRING("随机字母数字", "Random String"),
    RANDOM_UUID_NO_DASH("随机UUID(无横线)", "UUID Without Dash");

    private final String label;
    private final String description;

    @Override
    public String toString() {
        return label;
    }

    public static DataGenTypeEnum findByLabel(String label) {
        for (DataGenTypeEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return UUID;
    }
}
