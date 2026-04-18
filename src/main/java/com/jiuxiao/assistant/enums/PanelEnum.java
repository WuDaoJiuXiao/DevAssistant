package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 该枚举类定义了系统的主要功能模块，每个模块都有对应的编码和名称
 *
 * @author 悟道九霄
 * @date 2026/4/18
 */
@Getter
@AllArgsConstructor
public enum PanelEnum {

    FORMAT_CONVERSION(0, "格式转换"),
    TEXT_PROCESSING(1, "文本处理"),
    SECURITY_CRYPTO(3, "安全加密"),
    DEV_ASSISTANT(4, "开发辅助"),
    ;

    private final Integer code;
    private final String panel;

    public static PanelEnum findByPanel(String panel) {
        for (PanelEnum panelEnum : PanelEnum.values()) {
            if (Objects.equals(panelEnum.panel, panel)) {
                return panelEnum;
            }
        }
        return null;
    }
}
