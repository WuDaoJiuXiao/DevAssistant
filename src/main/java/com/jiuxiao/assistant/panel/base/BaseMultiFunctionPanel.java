package com.jiuxiao.assistant.panel.base;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * 多功能工具面板基类
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public abstract class BaseMultiFunctionPanel extends JPanel {

    protected JTextArea inputArea;
    protected JTextArea outputArea;
    protected JComboBox<String> functionCombo;
    protected JPanel dynamicButtonPanel;
    protected JButton executeButton;
    protected JButton swapButton;

    protected boolean showSwapButton = true;
    protected String panelTitle = "工具";

    public BaseMultiFunctionPanel() {
        UIManager.put("Label.font", new Font(null, Font.PLAIN, 11));
        UIManager.put("Button.font", new Font(null, Font.PLAIN, 11));
        UIManager.put("TextField.font", new Font(null, Font.PLAIN, 11));
        UIManager.put("CheckBox.font", new Font(null, Font.PLAIN, 11));
        UIManager.put("RadioButton.font", new Font(null, Font.PLAIN, 11));
        UIManager.put("ComboBox.font", new Font(null, Font.PLAIN, 11));
        initUI();
    }

    /**
     * 初始化UI布局
     * 使用BorderLayout布局管理器，分为三个主要部分：
     * 1. 北部：输入区域
     * 2. 中部：功能区域
     * 3. 南部：输出区域
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(JBUI.Borders.empty(10));

        // 创建三部分
        JPanel inputPanel = createInputPanel();
        JPanel functionPanel = createFunctionPanel();
        JPanel outputPanel = createOutputPanel();

        // 将三个部分添加到面板中
        add(inputPanel, BorderLayout.NORTH);
        add(functionPanel, BorderLayout.CENTER);
        add(outputPanel, BorderLayout.SOUTH);

        // 设置面板大小
        setPreferredSize(new Dimension(500, 400));
    }

    /**
     * 创建输入区域
     * 创建一个带标题的文本输入区域，支持滚动
     */
    protected JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        // 创建带标题的边框
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.GRAY),
                "输入",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));


        // 创建文本输入区域
        inputArea = new JTextArea(9, 40);
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        // 添加滚动面板
        JScrollPane scrollPane = new JBScrollPane(inputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建功能区
     * 包含功能选择下拉框、执行按钮和动态按钮面板
     */
    protected JPanel createFunctionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.GRAY),
                "功能",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));

        // 顶部面板：左侧功能选择 + 右侧按钮
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(JBUI.Borders.empty(5));

        // 左侧：功能选择
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.add(new JLabel("选择功能:"));
        functionCombo = new ComboBox<>();
        populateFunctionCombo(functionCombo);
        functionCombo.addActionListener(e -> onFunctionChanged());
        leftPanel.add(functionCombo);
        topPanel.add(leftPanel, BorderLayout.WEST);

        // 右侧：按钮面板
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        executeButton = new JButton("执行");
        executeButton.addActionListener(e -> execute());
        rightPanel.add(executeButton);

        // 根据配置决定是否显示交换按钮
        if (showSwapButton) {
            swapButton = new JButton("交换输入/输出");
            swapButton.addActionListener(e -> swapInputOutput());
            rightPanel.add(swapButton);
        }
        topPanel.add(rightPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        // 中间：动态按钮面板
        dynamicButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dynamicButtonPanel.setBackground(null);
        panel.add(dynamicButtonPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建输出区域
     * 创建一个只读的文本输出区域，支持滚动
     */
    protected JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.GRAY),
                "输出",
                TitledBorder.LEFT,
                TitledBorder.TOP
        ));

        // 创建文本输出区域
        outputArea = new JTextArea(9, 40);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);
        outputArea.setBackground(new JBColor(Gray._250, Gray._50));

        // 添加滚动面板
        JScrollPane scrollPane = new JBScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 功能变化时的回调方法
     * 子类需要实现此方法以处理功能切换时的逻辑
     */
    protected abstract void onFunctionChanged();

    /**
     * 填充功能下拉框
     * 子类需要实现此方法以填充可用的功能列表
     */
    protected abstract void populateFunctionCombo(JComboBox<String> combo);

    /**
     * 执行当前功能
     * 子类需要实现此方法以执行具体的功能逻辑
     */
    protected abstract void execute();

    /**
     * 获取当前选中的功能
     *
     * @return 当前选中的功能名称
     */
    protected String getCurrentFunction() {
        return (String) functionCombo.getSelectedItem();
    }

    /**
     * 验证输入内容
     * 检查输入是否为空
     *
     * @return 验证通过返回true，否则返回false
     */
    protected boolean validateInput() {
        String input = inputArea.getText().trim();
        return StringUtils.isBlank(input);
    }

    /**
     * 设置输出结果
     *
     * @param result 要显示的结果文本
     */
    protected void setOutput(String result) {
        outputArea.setText(result);
    }

    /**
     * 获取输入文本
     *
     * @return 输入区域的文本内容（去除首尾空格）
     */
    protected String getInput() {
        return inputArea.getText().trim();
    }

    /**
     * 交换输入输出
     */
    protected void swapInputOutput() {
        String input = inputArea.getText();
        String output = outputArea.getText();
        inputArea.setText(output);
        outputArea.setText(input);
    }
}