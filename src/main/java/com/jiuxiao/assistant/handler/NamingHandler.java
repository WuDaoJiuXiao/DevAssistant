package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.NamingTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * 命名处理器类，用于处理各种命名风格的转换
 * 提供了创建配置面板、执行命名转换、生成各种命名格式等功能
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
public class NamingHandler {

    private JComboBox<NamingTypeEnum> namingTypeCombo;

    /**
     * 创建并返回一个配置面板，包含命名类型选择下拉框
     * 使用GridBagLayout进行布局，设置适当的边距和填充方式
     *
     * @return 配置面板JPanel，包含命名类型选择的下拉框
     */
    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("命名类型:"), gbc);

        namingTypeCombo = new ComboBox<>(NamingTypeEnum.values());
        namingTypeCombo.setSelectedItem(NamingTypeEnum.ALL);
        gbc.gridx = 1;
        panel.add(namingTypeCombo, gbc);

        return panel;
    }

    /**
     * 执行命名转换
     * 根据选择的命名类型将输入字符串转换为相应的命名风格
     *
     * @param input 输入的字符串
     * @return 转换后的命名字符串
     * @throws Exception 当输入为空时抛出异常
     */
    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        NamingTypeEnum selectedType = (NamingTypeEnum) namingTypeCombo.getSelectedItem();
        if (Objects.isNull(selectedType)) return getExample();

        switch (selectedType) {
            case CAMEL:
                return toCamelCase(input);
            case SNAKE:
                return toSnakeCase(input);
            case KEBAB:
                return toKebabCase(input);
            case PASCAL:
                return toPascalCase(input);
            case CONSTANT:
                return toSnakeCase(input).toUpperCase();
            case ALL:
            default:
                return generateAll(input);
        }
    }

    /**
     * 生成所有命名风格的字符串
     * 将输入字符串转换为所有支持的命名风格并格式化输出
     *
     * @param input 输入的字符串
     * @return 包含所有命名风格的字符串，每种风格一行
     */
    private String generateAll(String input) {
        String camel = toCamelCase(input);
        String pascal = toPascalCase(input);
        String snake = toSnakeCase(input);
        String constant = snake.toUpperCase();
        String kebab = toKebabCase(input);

        return "驼峰命名: " + camel + "\n" +
                "帕斯卡命名: " + pascal + "\n" +
                "蛇形命名: " + snake + "\n" +
                "常量命名: " + constant + "\n" +
                "短横线命名: " + kebab;
    }

    /**
     * 转换为驼峰命名法
     * 例如: user_name -> userName
     *
     * @param str 输入字符串
     * @return 驼峰命名字符串
     */
    private String toCamelCase(String str) {
        String[] parts = str.split("[_-]+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                sb.append(parts[i].toLowerCase());
            } else {
                sb.append(capitalize(parts[i].toLowerCase()));
            }
        }
        return sb.toString();
    }

    /**
     * 转换为帕斯卡命名法
     * 例如: user_name -> UserName
     *
     * @param str 输入字符串
     * @return 帕斯卡命名字符串
     */
    private String toPascalCase(String str) {
        String[] parts = str.split("[_-]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(capitalize(part.toLowerCase()));
        }
        return sb.toString();
    }

    /**
     * 转换为蛇形命名法
     * 例如: userName -> user_name
     *
     * @param str 输入字符串
     * @return 蛇形命名字符串
     */
    private String toSnakeCase(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 转换为短横线命名法
     * 例如: userName -> user-name
     *
     * @param str 输入字符串
     * @return 短横线命名字符串
     */
    private String toKebabCase(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('-');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串首字母大写
     *
     * @param str 输入字符串
     * @return 首字母大写的字符串
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 获取默认示例
     *
     * @return 默认示例字符串
     */
    public String getExample() {
        return "user_name";
    }
}
