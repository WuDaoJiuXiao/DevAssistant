package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrEncodeHandler {

    private JComboBox<String> encodeTypeCombo;
    private JTextField secretKeyField;

    private static final String[] ENCODE_TYPES = {
        "Base64编码",
        "URL编码",
        "Hex编码",
        "MD5摘要",
        "SHA-256摘要",
        "SHA-512摘要"
    };

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("加密/编码类型:"), gbc);

        encodeTypeCombo = new JComboBox<>(ENCODE_TYPES);
        encodeTypeCombo.setSelectedIndex(0);
        gbc.gridx = 1;
        panel.add(encodeTypeCombo, gbc);

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
        String selectedType = (String) encodeTypeCombo.getSelectedItem();
        String secretKey = secretKeyField.getText();

        try {
            switch (selectedType) {
                case "Base64编码":
                    return base64Encode(input);
                case "URL编码":
                    return urlEncode(input);
                case "Hex编码":
                    return hexEncode(input);
                case "MD5摘要":
                    return md5(input);
                case "SHA-256摘要":
                    return sha256(input);
                case "SHA-512摘要":
                    return sha512(input);
                default:
                    return "未知的编码类型";
            }
        } catch (Exception e) {
            throw new Exception("编码失败: " + e.getMessage());
        }
    }

    private String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String urlEncode(String input) {
        return java.net.URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    private String hexEncode(String input) throws Exception {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String md5(String input) throws Exception {
        return hash(input, "MD5");
    }

    private String sha256(String input) throws Exception {
        return hash(input, "SHA-256");
    }

    private String sha512(String input) throws Exception {
        return hash(input, "SHA-512");
    }

    private String hash(String input, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] hashBytes = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String getExample() {
        return "Hello World";
    }
}
