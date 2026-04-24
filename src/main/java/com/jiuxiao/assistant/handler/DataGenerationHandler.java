package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class DataGenerationHandler {

    private JComboBox<String> dataTypeCombo;
    private JTextField countField;
    private JTextField lengthField;
    private JCheckBox includeSymbolsCheckBox;

    private static final String[] DATA_TYPES = {
        "UUID",
        "雪花ID",
        "时间戳(毫秒)",
        "时间戳(秒)",
        "随机整数",
        "随机字母数字",
        "随机UUID(无横线)"
    };

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("数据类型:"), gbc);

        dataTypeCombo = new JComboBox<>(DATA_TYPES);
        dataTypeCombo.setSelectedIndex(0);
        dataTypeCombo.addActionListener(e -> updatePanelState());
        gbc.gridx = 1;
        panel.add(dataTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("生成数量:"), gbc);

        countField = new JTextField("1", 10);
        gbc.gridx = 1;
        panel.add(countField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("生成位数:"), gbc);

        lengthField = new JTextField("16", 10);
        gbc.gridx = 1;
        panel.add(lengthField, gbc);

        includeSymbolsCheckBox = new JCheckBox("包含常用符号", false);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(includeSymbolsCheckBox, gbc);

        updatePanelState();

        return panel;
    }

    private void updatePanelState() {
        String selected = (String) dataTypeCombo.getSelectedItem();
        boolean showOptions = "随机字母数字".equals(selected);
        lengthField.setEnabled(showOptions);
        includeSymbolsCheckBox.setEnabled(showOptions);
    }

    public String execute(String input) throws Exception {
        String selectedType = (String) dataTypeCombo.getSelectedItem();
        int count;

        try {
            String countText = countField.getText().trim();
            if (countText.isEmpty()) {
                count = 1;
            } else {
                count = Integer.parseInt(countText);
            }
            if (count <= 0 || count > 1000) {
                count = 1;
            }
        } catch (NumberFormatException e) {
            count = 1;
        }

        StringBuilder sb = new StringBuilder();

        switch (selectedType) {
            case "UUID":
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(UUID.randomUUID().toString());
                }
                break;
            case "雪花ID":
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(generateSnowflakeId());
                }
                break;
            case "时间戳(毫秒)":
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(System.currentTimeMillis());
                }
                break;
            case "时间戳(秒)":
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(System.currentTimeMillis() / 1000);
                }
                break;
            case "随机整数":
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append((int) (Math.random() * 100000));
                }
                break;
            case "随机字母数字":
                int length;
                try {
                    String lengthText = lengthField.getText().trim();
                    if (lengthText.isEmpty()) {
                        length = 16;
                    } else {
                        length = Integer.parseInt(lengthText);
                    }
                    if (length <= 0 || length > 200) {
                        length = 16;
                    }
                } catch (NumberFormatException e) {
                    length = 16;
                }

                boolean includeSymbols = includeSymbolsCheckBox.isSelected();
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(generateRandomString(length, includeSymbols));
                }
                break;
            case "随机UUID(无横线)":
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(UUID.randomUUID().toString().replace("-", ""));
                }
                break;
        }

        return sb.toString();
    }

    private long generateSnowflakeId() {
        long timestamp = System.currentTimeMillis() - 1609459200000L;
        long workerId = 1L;
        long sequence = 0L;

        return (timestamp << 22) | (workerId << 12) | sequence;
    }

    private String generateRandomString(int length, boolean includeSymbols) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        if (includeSymbols) {
            chars += "!@#$%^&*()-_=+[]{}|;:,.<>?";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public String getExample() {
        return "";
    }
}
