package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class TrimSpaceHandler {

    private JCheckBox removeAllSpaceCheckBox;
    private JCheckBox removeNewlineCheckBox;
    private JCheckBox trimOnlyCheckBox;

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        removeAllSpaceCheckBox = new JCheckBox("去除所有空格", true);
        removeNewlineCheckBox = new JCheckBox("去除换行符", true);
        trimOnlyCheckBox = new JCheckBox("首尾去空格", false);

        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        checkPanel.setBackground(null);
        checkPanel.add(removeAllSpaceCheckBox);
        checkPanel.add(removeNewlineCheckBox);
        checkPanel.add(trimOnlyCheckBox);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(checkPanel, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        String result = input;

        if (removeAllSpaceCheckBox.isSelected()) {
            result = result.replaceAll("\\s+", "");
        }

        if (removeNewlineCheckBox.isSelected()) {
            result = result.replaceAll("[\n\r]+", "");
        }

        if (trimOnlyCheckBox.isSelected()) {
            result = result.trim();
        }

        return result;
    }

    public String getExample() {
        return "  Hello   World  \n  Test  ";
    }
}
