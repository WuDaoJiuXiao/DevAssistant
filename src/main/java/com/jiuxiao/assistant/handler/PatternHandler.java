package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.PatternTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * PatternHandler 类用于处理正则表达式相关的操作
 * 它创建一个包含正则表达式选择下拉框的面板，并提供执行正则匹配的功能
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
public class PatternHandler {

    /**
     * 用于显示和选择正则表达式类型的下拉框组件
     */
    private JComboBox<PatternTypeEnum> patternCombo;

    /**
     * 创建一个包含正则表达式选择下拉框的面板
     *
     * @return 配置好的JPanel面板
     */
    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("选择正则:"), gbc);

        patternCombo = new ComboBox<>(PatternTypeEnum.values());
        patternCombo.setSelectedItem(PatternTypeEnum.PHONE);
        gbc.gridx = 1;
        panel.add(patternCombo, gbc);

        return panel;
    }

    /**
     * 执行正则表达式匹配
     *
     * @param input 需要验证的输入字符串
     * @return 包含正则表达式和匹配结果的字符串
     * @throws Exception 如果发生异常
     */
    public String execute(String input) throws Exception {
        // 获取当前选中的正则表达式类型
        PatternTypeEnum selectedPattern = (PatternTypeEnum) patternCombo.getSelectedItem();
        if (Objects.isNull(selectedPattern)) return getExample();

        String regex = selectedPattern.getRegex();
        if (input == null || input.trim().isEmpty()) {
            return "正则表达式: " + regex;
        }

        input = input.trim();
        boolean matches = input.matches(regex);

        return "正则表达式: " + regex + "\n" +
                "验证结果: " + (matches ? "✅ 匹配" : "❌ 不匹配");
    }

    /**
     * 获取正则表达式的示例
     *
     * @return 示例字符串，当前实现返回空字符串
     */
    public String getExample() {
        return "";
    }
}
