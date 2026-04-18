package com.jiuxiao.assistant.listener;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;
import com.jiuxiao.assistant.service.MapperXmlCacheService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 项目启动后的监听器
 * 该类实现了ApplicationActivationListener接口，用于监听IDE应用程序的激活事件
 * 当项目激活时，会初始化Mapper XML缓存服务
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public class AssistantStartUpListener implements ApplicationActivationListener {

    /**
     * 应用程序激活时的处理方法
     * 当IDE窗口被激活时触发，用于初始化Mapper XML缓存服务
     *
     * @param ideFrame IDE窗口框架对象，包含当前项目信息
     */
    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        // 获取当前项目对象
        Project project = ideFrame.getProject();
        // 检查项目是否为空或已关闭，如果是则直接返回
        if (Objects.isNull(project) || project.isDisposed()) {
            return;
        }

        // 延迟初始化，等待项目完全加载和索引完成
        ApplicationManager.getApplication().invokeLater(() -> {
            // 再次检查项目是否已关闭，避免在等待期间项目被关闭
            if (project.isDisposed()) return;

            // 等待索引就绪后执行初始化操作
            com.intellij.openapi.project.DumbService.getInstance(project).runWhenSmart(() -> {
                // 在读模式下执行初始化操作，避免阻塞其他操作
                ApplicationManager.getApplication().runReadAction(() -> {
                    try {
                        // 获取MapperXmlCacheService实例
                        MapperXmlCacheService mapperXmlCacheService = project.getService(MapperXmlCacheService.class);
                        // 检查服务实例是否有效，如果有效则初始化Mapper XML映射
                        if (Objects.nonNull(mapperXmlCacheService)) {
                            // 使用增量初始化而不是全量，提高初始化效率
                            mapperXmlCacheService.initMapperXmlMap();
                        }
                    } catch (Exception e) {
                        // 记录初始化失败的警告日志
                        com.intellij.openapi.diagnostic.Logger.getInstance(AssistantStartUpListener.class)
                                .warn("Failed to init Mapper XML cache", e);
                    }
                });
            });
        });
    }
}