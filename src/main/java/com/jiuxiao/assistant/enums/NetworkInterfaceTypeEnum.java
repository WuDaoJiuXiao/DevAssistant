package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 网络接口类型枚举
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
@Getter
@AllArgsConstructor
public enum NetworkInterfaceTypeEnum {
    VMWARE("VMware", "VMware虚拟机"),
    VIRTUALBOX("VirtualBox", "VirtualBox虚拟机"),
    DOCKER("Docker", "Docker容器"),
    LOOPBACK("Loopback", "回环接口");

    private final String keyword;
    private final String description;

    public static boolean shouldSkip(String interfaceName) {
        if (interfaceName == null) {
            return true;
        }
        for (NetworkInterfaceTypeEnum type : values()) {
            if (interfaceName.contains(type.keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldSkipLoopback(boolean isLoopback) {
        return isLoopback;
    }
}