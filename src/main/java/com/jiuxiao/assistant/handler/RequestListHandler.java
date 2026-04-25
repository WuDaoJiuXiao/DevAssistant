package com.jiuxiao.assistant.handler;

import com.jiuxiao.assistant.dto.controller.RequestMappingInfo;
import com.jiuxiao.assistant.service.ControllerRequestCacheService;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * 请求列表处理器类，用于处理请求列表的获取和导航功能
 *
 * @author 悟道九霄
 * @date 2026/4/18
 */
public class RequestListHandler {

    private ControllerRequestCacheService cacheService;

    /**
     * 设置控制器请求缓存服务
     *
     * @param cacheService 控制器请求缓存服务实例
     */
    public void setCacheService(ControllerRequestCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 获取指定标签页和搜索条件下的行数据
     *
     * @param tabIndex   标签页索引（当前未使用）
     * @param searchText 搜索文本，为空或null时获取全部数据
     * @return 包含请求方法和URL的Vector<Vector < Object>>数据
     */
    public Vector<Vector<Object>> getRowData(int tabIndex, String searchText) {
        if (cacheService == null) {
            return new Vector<>();
        }
        List<RequestMappingInfo> infoList;
        if (searchText == null || searchText.trim().isEmpty()) {
            infoList = cacheService.getRequestMappingInfoList();
        } else {
            infoList = cacheService.searchRequestMapping(searchText.trim());
        }

        return infoList.stream()
                .map(info -> {
                    Vector<Object> row = new Vector<>();
                    row.add(info.getRequestMethod());
                    row.add(info.getUrl());
                    return row;
                })
                .collect(Collectors.toCollection(Vector::new));
    }

    /**
     * 根据URL导航到对应的方法
     *
     * @param url 要导航的URL地址
     */
    public void navigateToUrl(String url) {
        if (cacheService == null || url == null) {
            return;
        }

        // 根据URL查找对应的RequestMappingInfo
        List<RequestMappingInfo> infoList = cacheService.getRequestMappingInfoList();
        for (RequestMappingInfo info : infoList) {
            if (url.equals(info.getUrl())) {
                cacheService.navigateToMethod(info);
                break;
            }
        }
    }
}
