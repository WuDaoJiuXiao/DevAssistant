package com.jiuxiao.assistant.dto.maven;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 网易Maven请求数据
 * @author 悟道九霄
 * @date 2026/5/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetEaseMavenRequestData {
    private Integer page = 1;
    private Integer start = 0;
    private Integer limit = 300;
    private List<SortParam> sort;
    private List<FilterParam> filter;

    @Data
    @NoArgsConstructor
    public static class SortParam {
        private final String property = "name";
        private final String direction = "ASC";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterParam {
        private String property;
        private String value;
    }
}
