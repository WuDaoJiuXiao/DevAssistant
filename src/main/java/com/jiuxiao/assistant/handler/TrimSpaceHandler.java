package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.TrimSpaceTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * 去空格处理器类
 * 提供了创建去空格面板和执行去空格操作的功能
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
public class TrimSpaceHandler {

    /**
     * 去空格类型下拉选择框
     */
    private JComboBox<TrimSpaceTypeEnum> typeCombo;

    /**
     * 创建去空格设置面板
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
        panel.add(new JLabel("去空格类型:"), gbc);

        typeCombo = new ComboBox<>(TrimSpaceTypeEnum.values());
        typeCombo.setSelectedItem(TrimSpaceTypeEnum.REMOVE_ALL);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        return panel;
    }

    /**
     * 执行去空格操作
     *
     * @param input 需要处理的输入字符串
     * @return 处理后的字符串
     * @throws Exception 当输入为空时抛出异常
     */
    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        TrimSpaceTypeEnum selectedType = (TrimSpaceTypeEnum) typeCombo.getSelectedItem();
        if (Objects.isNull(selectedType)) return getExample();

        String result = input;
        if (!selectedType.getSpaceRegex().isEmpty()) {
            result = result.replaceAll(selectedType.getSpaceRegex(), "");
        }

        if ("trim".equals(selectedType.getTrimType())) {
            result = result.trim();
        }

        return result;
    }

    /**
     * 获取示例字符串
     *
     * @return 返回一个包含空格与换行符的字符串
     */
    public String getExample() {
        return "  Hello   World  \n  Test  ";
    }
}