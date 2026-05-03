package com.jiuxiao.assistant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maven镜像仓库枚举类
 *
 * @author 悟道九霄
 * @date 2026/5/3
 */
@Getter
@AllArgsConstructor
public enum MavenRepositoryEnum {

    MAVEN_CENTER(1, "MavenCenter", "Maven中心仓", "https://search.maven.org/solrsearch/select?q=a:%s&rows=%d&start=%d&wt=json"),
    ALI_YUN(2, "AliYun", "阿里云", "https://maven.aliyun.com/artifact/aliyunMaven/searchArtifactByGav"),
    NET_EASE(3, "NetEase", "网易", "http://mirrors.163.com/maven/service/extdirect");

    private final Integer id;
    private final String name;
    private final String desc;
    private final String url;

    public static List<String> descList() {
        return Arrays.stream(MavenRepositoryEnum.values())
                .map(MavenRepositoryEnum::getDesc).collect(Collectors.toList());
    }

    public static MavenRepositoryEnum findByDesc(String desc) {
        for (MavenRepositoryEnum repositoryEnum : MavenRepositoryEnum.values()) {
            if (Objects.equals(repositoryEnum.desc, desc)) {
                return repositoryEnum;
            }
        }
        return null;
    }
}
