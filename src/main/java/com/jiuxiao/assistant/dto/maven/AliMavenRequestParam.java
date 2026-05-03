package com.jiuxiao.assistant.dto.maven;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * 阿里云Maven请求参数类
 * @author 悟道九霄
 * @date 2026/5/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliMavenRequestParam {
    private String groupId;
    private String artifactId;
    private String version;
    private String reopId = "all";

    public String buildRequestUrl() {
        return "?" + "groupId=" + Optional.ofNullable(groupId).orElse("") + "&"
                + "artifactId=" + Optional.ofNullable(artifactId).orElse("") + "&"
                + "version=" + Optional.ofNullable(version).orElse("") + "&repoId=all&_input_charset=utf-8";
    }
}
