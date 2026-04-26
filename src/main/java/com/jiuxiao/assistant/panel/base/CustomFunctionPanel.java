package com.jiuxiao.assistant.panel.base;

import com.intellij.openapi.project.Project;
import com.jiuxiao.assistant.panel.MavenPanel;
import com.jiuxiao.assistant.panel.RequestListPanel;

/**
 * 自定义功能主面板
 * 这是 BaseCustomPanel 的具体实现，负责注册所有子功能
 * 
 * @author 悟道九霄
 * @date 2026/4/26
 */
public class CustomFunctionPanel extends BaseCustomPanel {
    
    public CustomFunctionPanel(Project project) {
        super(project);
    }
    
    @Override
    protected void registerSubPanels() {
        // 在这里注册所有子功能面板
        registerSubPanel("项目请求列表", new RequestListPanel(project));
        registerSubPanel("Maven依赖", new MavenPanel(project));
    }
}