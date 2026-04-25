package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.DecodeTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * 字符串解码处理器类
 * 提供创建解码面板和执行解码功能
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
public class StrDecodeHandler {

    /**
     * 解码类型下拉选择框
     */
    private JComboBox<DecodeTypeEnum> decodeTypeCombo;

    /**
     * 创建解码面板
     *
     * @return 返回一个包含解码类型选择和密钥输入的JPanel
     */
    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("解密/解码类型:"), gbc);

        decodeTypeCombo = new ComboBox<>(DecodeTypeEnum.values());
        decodeTypeCombo.setSelectedItem(DecodeTypeEnum.BASE64);
        gbc.gridx = 1;
        panel.add(decodeTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("密钥(可选):"), gbc);

        JTextField secretKeyField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(secretKeyField, gbc);

        return panel;
    }

    /**
     * 执行解码操作
     *
     * @param input 需要解码的字符串
     * @return 解码后的字符串
     * @throws Exception 当输入为空或解码失败时抛出异常
     */
    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        DecodeTypeEnum selectedType = (DecodeTypeEnum) decodeTypeCombo.getSelectedItem();
        if (Objects.isNull(selectedType)) return getExample();

        try {
            switch (selectedType) {
                case BASE64:
                    return base64Decode(input);
                case URL:
                    return urlDecode(input);
                case HEX:
                    return hexDecode(input);
                default:
                    return "未知的解码类型";
            }
        } catch (Exception e) {
            throw new Exception("解码失败: " + e.getMessage());
        }
    }

    /**
     * Base64解码方法
     *
     * @param input Base64编码的字符串
     * @return 解码后的字符串
     */
    private String base64Decode(String input) {
        byte[] bytes = Base64.getDecoder().decode(input);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * URL解码方法
     *
     * @param input URL编码的字符串
     * @return 解码后的字符串
     */
    private String urlDecode(String input) {
        return java.net.URLDecoder.decode(input, StandardCharsets.UTF_8);
    }

    /**
     * 十六进制解码方法
     *
     * @param input 十六进制编码的字符串
     * @return 解码后的字符串
     * @throws Exception 当十六进制字符串长度不是偶数时抛出异常
     */
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

    /**
     * 获取示例字符串
     *
     * @return 返回一个Base64编码的示例字符串
     */
    public String getExample() {
        return "SGVsbG8gV29ybGQ=";
    }
}
