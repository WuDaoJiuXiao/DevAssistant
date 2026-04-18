package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author 悟道九霄
 * @date 2026/4/18
 */
@Getter
@AllArgsConstructor
public enum HandlerEnum {

    JSON_SYNC_JAVA(0, "JSON ↔ Java", PanelEnum.FORMAT_CONVERSION),
    JSON_SYNC_XML(1, "JSON ↔ XML", PanelEnum.FORMAT_CONVERSION),
    PAYLOAD_TO_JSON(2, "负载转JSON", PanelEnum.FORMAT_CONVERSION),
    TIMESTAMP_SYNC_DATE(3, "时间戳 ↔ 日期", PanelEnum.FORMAT_CONVERSION),
    BINARY_SYNC(4, "进制转换", PanelEnum.FORMAT_CONVERSION),

    NAMING_SYNC(5, "命名互转", PanelEnum.TEXT_PROCESSING),
    TRIM_SPACE(6, "去空格/换行", PanelEnum.TEXT_PROCESSING),

    STR_ENCODE(7, "字符串加密/编码", PanelEnum.SECURITY_CRYPTO),
    STR_DECODE(8, "字符串解密/解码", PanelEnum.SECURITY_CRYPTO),

    PATTERN(9, "常用正则", PanelEnum.DEV_ASSISTANT),
    DATA_GENERATION(10, "常用数据生成", PanelEnum.DEV_ASSISTANT),
    MACHINE_INFORMATION(11, "机器IP信息", PanelEnum.DEV_ASSISTANT),
    REQUEST_LIST(12, "项目请求列表", PanelEnum.DEV_ASSISTANT),
    MAVEN_DEPENDENCY(13, "Maven依赖", PanelEnum.DEV_ASSISTANT),
    MYBATIS_SQL(14, "Mybatis SQL 还原", PanelEnum.DEV_ASSISTANT),

    ;
    private final Integer code;
    private final String function;
    private final PanelEnum parentPanel;

    public static HandlerEnum findByFunction(String function) {
        for (HandlerEnum handlerEnum : HandlerEnum.values()) {
            if (Objects.equals(handlerEnum.function, function)) {
                return handlerEnum;
            }
        }
        return null;
    }
}
