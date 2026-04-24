package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class StrDecodeHandler {

    private JComboBox<String> decodeTypeCombo;
    private JTextField secretKeyField;

    private static final String[] DECODE_TYPES = {
        "Base64解码",
        "URL解码",
        "Hex解码"
    };

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("解密/解码类型:"), gbc);

        decodeTypeCombo = new JComboBox<>(DECODE_TYPES);
        decodeTypeCombo.setSelectedIndex(0);
        gbc.gridx = 1;
        panel.add(decodeTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("密钥(可选):"), gbc);

        secretKeyField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(secretKeyField, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        String selectedType = (String) decodeTypeCombo.getSelectedItem();

        try {
            switch (selectedType) {
                case "Base64解码":
                    return base64Decode(input);
                case "URL解码":
                    return urlDecode(input);
                case "Hex解码":
                    return hexDecode(input);
                default:
                    return "未知的解码类型";
            }
        } catch (Exception e) {
            throw new Exception("解码失败: " + e.getMessage());
        }
    }

    private String base64Decode(String input) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(input);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String urlDecode(String input) throws Exception {
        return java.net.URLDecoder.decode(input, StandardCharsets.UTF_8);
    }

    private String hexDecode(String input) throws Exception {
        input = input.replaceAll("\\s+", "");
        if (input.length() % 2 != 0) {
            throw new Exception("Hex字符串长度必须是偶数");
        }
        byte[] bytes = new byte[input.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            bytes[i] = (byte) Integer.parseInt(input.substring(index, index + 2), 16);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String getExample() {
        return "SGVsbG8gV29ybGQ=";
    }
}
