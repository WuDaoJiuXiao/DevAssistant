package com.jiuxiao.assistant.dto.maven;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 网易Maven请求体
 * @author 悟道九霄
 * @date 2026/5/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetEaseMavenRequestBody {
    private String action = "coreui_Search";
    private String method = "read";
    private List<NetEaseMavenRequestData> data;
    private String type = "rpc";
    private Integer tid = 1;
}
