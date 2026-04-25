package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 正则类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
@Getter
@AllArgsConstructor
public enum PatternTypeEnum {
    PHONE("手机号码", "^1[3-9]\\d{9}$"),
    EMAIL("邮箱", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"),
    ID_CARD("身份证号", "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dxX]$"),
    IP_ADDRESS("IP地址", "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"),
    URL("URL", "^https?://[\\w.-]+(:\\d+)?(/[\\w./-]*)?$"),
    DATE_YMD("日期格式(yyyy-MM-dd)", "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"),
    DATE_SLASH("日期格式(yyyy/MM/dd)", "^\\d{4}/(0[1-9]|1[0-2])/(0[1-9]|[12]\\d|3[01])$"),
    DATE_DOT("日期格式(yyyy.MM.dd)", "^\\d{4}.(0[1-9]|1[0-2]).(0[1-9]|[12]\\d|3[01])$"),
    DATE_COMPACT("日期格式(yyyyMMdd)", "^\\d{8}$"),
    TIME_HMS("时间格式(HH:mm:ss)", "^(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$"),
    DATETIME("日期时间格式", "^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}$"),
    CHINESE("中文", "^[\\u4e00-\\u9fa5]+$"),
    NUMBER("数字", "^\\d+$"),
    LETTER("字母", "^[a-zA-Z]+$"),
    ALPHANUMERIC("字母数字组合", "^[a-zA-Z0-9]+$"),
    POSTCODE("邮编", "^\\d{6}$"),
    BANK_CARD("银行卡号", "^\\d{16,19}$");

    private final String label;
    private final String regex;

    @Override
    public String toString() {
        return label;
    }

    public static PatternTypeEnum findByLabel(String label) {
        for (PatternTypeEnum typeEnum : values()) {
            if (typeEnum.label.equals(label)) {
                return typeEnum;
            }
        }
        return PHONE;
    }
}
