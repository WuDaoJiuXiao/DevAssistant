package com.jiuxiao.assistant.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jiuxiao.assistant.enums.PanelEnum;
import com.jiuxiao.assistant.panel.DataConversionPanel;
import com.jiuxiao.assistant.panel.SecurityCryptoPanel;
import com.jiuxiao.assistant.panel.TextProcessingPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 该类负责创建和管理插件的工具窗口界面
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public class AssistantToolWindowFactory implements ToolWindowFactory {

    /**
     * 创建工具窗口内容
     * 当工具窗口首次显示时调用此方法
     *
     * @param project    当前项目对象
     * @param toolWindow 工具窗口对象
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建主面板，用于容纳不同的功能标签页
        JTabbedPane mainTabbedPane = new JBTabbedPane();

        // 添加数据转换Tab（聚合多个功能）
        DataConversionPanel dataConversionPanel = new DataConversionPanel(project);
        TextProcessingPanel textProcessingPanel = new TextProcessingPanel(project);
        SecurityCryptoPanel securityCryptoPanel = new SecurityCryptoPanel(project);
        mainTabbedPane.addTab(PanelEnum.FORMAT_CONVERSION.getPanel(), dataConversionPanel);
        mainTabbedPane.addTab(PanelEnum.TEXT_PROCESSING.getPanel(), textProcessingPanel);
        mainTabbedPane.addTab(PanelEnum.SECURITY_CRYPTO.getPanel(), securityCryptoPanel);

        // 添加到ToolWindow
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainTabbedPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}