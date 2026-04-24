package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class NamingHandler {

    private JRadioButton allInOneRadio;
    private JRadioButton camelRadio;
    private JRadioButton snakeRadio;
    private JRadioButton kebabRadio;

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        allInOneRadio = new JRadioButton("生成所有命名", true);
        camelRadio = new JRadioButton("驼峰");
        snakeRadio = new JRadioButton("蛇形");
        kebabRadio = new JRadioButton("短横线");

        ButtonGroup group = new ButtonGroup();
        group.add(allInOneRadio);
        group.add(camelRadio);
        group.add(snakeRadio);
        group.add(kebabRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setBackground(null);
        radioPanel.add(allInOneRadio);
        radioPanel.add(camelRadio);
        radioPanel.add(snakeRadio);
        radioPanel.add(kebabRadio);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        panel.add(radioPanel, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();

        if (camelRadio.isSelected()) {
            return toCamelCase(input);
        } else if (snakeRadio.isSelected()) {
            return toSnakeCase(input);
        } else if (kebabRadio.isSelected()) {
            return toKebabCase(input);
        } else {
            return generateAll(input);
        }
    }

    private String generateAll(String input) {
        String camel = toCamelCase(input);
        String pascal = toPascalCase(input);
        String snake = toSnakeCase(input);
        String upperSnake = snake.toUpperCase();
        String kebab = toKebabCase(input);

        return "驼峰命名 (camelCase): " + camel + "\n" +
               "帕斯卡命名 (PascalCase): " + pascal + "\n" +
               "蛇形命名 (snake_case): " + snake + "\n" +
               "常量命名 (CONSTANT_CASE): " + upperSnake + "\n" +
               "短横线命名 (kebab-case): " + kebab;
    }

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

    private String toPascalCase(String str) {
        String[] parts = str.split("[_-]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(capitalize(part.toLowerCase()));
        }
        return sb.toString();
    }

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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public String getExample() {
        return "user_name";
    }
}
