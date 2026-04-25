package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.EncodeTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;

/**
 * 字符串编码处理器
 * 提供多种字符串编码和加密功能，包括Base64、URL编码、十六进制编码、MD5、SHA-256和SHA-512等
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
public class StrEncodeHandler {

    private JComboBox<EncodeTypeEnum> encodeTypeCombo;

    /**
     * 创建编码处理面板
     *
     * @return 返回一个包含编码类型选择和密钥输入的JPanel
     */
    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("加密/编码类型:"), gbc);

        encodeTypeCombo = new ComboBox<>(EncodeTypeEnum.values());
        encodeTypeCombo.setSelectedItem(EncodeTypeEnum.BASE64);
        gbc.gridx = 1;
        panel.add(encodeTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("密钥(可选):"), gbc);

        JTextField secretKeyField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(secretKeyField, gbc);

        return panel;
    }

    /**
     * 执行字符串编码
     *
     * @param input 需要编码的字符串
     * @return 编码后的字符串
     * @throws Exception 当输入为空或编码失败时抛出异常
     */
    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        EncodeTypeEnum selectedType = (EncodeTypeEnum) encodeTypeCombo.getSelectedItem();
        if (Objects.isNull(selectedType)) return getExample();

        try {
            switch (selectedType) {
                case BASE64:
                    return base64Encode(input);
                case URL:
                    return urlEncode(input);
                case HEX:
                    return hexEncode(input);
                case MD5:
                    return hash(input, "MD5");
                case SHA256:
                    return hash(input, "SHA-256");
                case SHA512:
                    return hash(input, "SHA-512");
                default:
                    return "未知的编码类型";
            }
        } catch (Exception e) {
            throw new Exception("编码失败: " + e.getMessage());
        }
    }

    /**
     * Base64编码
     *
     * @param input 需要编码的字符串
     * @return Base64编码后的字符串
     */
    private String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * URL编码
     *
     * @param input 需要编码的字符串
     * @return URL编码后的字符串
     */
    private String urlEncode(String input) {
        return java.net.URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    /**
     * 十六进制编码
     *
     * @param input 需要编码的字符串
     * @return 十六进制编码后的字符串
     */
    private String hexEncode(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 哈希加密
     *
     * @param input     需要加密的字符串
     * @param algorithm 加密算法名称
     * @return 加密后的字符串
     * @throws Exception 当加密算法不可用时抛出异常
     */
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

    /**
     * 获取示例字符串
     *
     * @return 返回"Hello World"作为示例
     */
    public String getExample() {
        return "Hello World";
    }
}
