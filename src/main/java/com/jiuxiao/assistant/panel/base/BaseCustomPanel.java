package com.jiuxiao.assistant.panel.base;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * @author 悟道九霄
 * @date 2026/4/26
 */
public abstract class BaseCustomPanel extends JPanel {

    protected Project project;
    protected JTabbedPane tabbedPane;

    public BaseCustomPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout(10, 10));
        setBorder(JBUI.Borders.empty(10));
        tabbedPane = new JBTabbedPane();
        registerSubPanels();
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * 注册所有子面板
     * 子类需要重写此方法来注册自己的功能面板
     */
    protected abstract void registerSubPanels();

    /**
     * 注册一个子面板到内部的标签页中
     *
     * @param tabName 标签页名称
     * @param panel 子面板实例（必须是 BaseCustomPanel 的子类）
     */
    protected void registerSubPanel(String tabName, JPanel panel) {
        tabbedPane.addTab(tabName, panel);
    }
}
