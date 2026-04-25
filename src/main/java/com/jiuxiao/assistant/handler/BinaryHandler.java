package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.NumBaseEnum;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * 二进制处理器类，用于处理不同进制之间的转换
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
public class BinaryHandler {

    /**
     * 源进制选择下拉框
     */
    private JComboBox<NumBaseEnum> fromBaseCombo;

    /**
     * 目标进制选择下拉框
     */
    private JComboBox<NumBaseEnum> toBaseCombo;

    /**
     * 创建并返回一个包含源进制和目标进制选择面板的JPanel
     *
     * @return 配置好的JPanel实例
     */
    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("源进制:"), gbc);

        fromBaseCombo = new ComboBox<>(NumBaseEnum.values());
        fromBaseCombo.setSelectedItem(NumBaseEnum.HEX);
        gbc.gridx = 1;
        panel.add(fromBaseCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("目标进制:"), gbc);

        toBaseCombo = new ComboBox<>(NumBaseEnum.values());
        toBaseCombo.setSelectedItem(NumBaseEnum.DECIMAL);
        gbc.gridx = 1;
        panel.add(toBaseCombo, gbc);

        return panel;
    }

    /**
     * 执行进制转换的核心方法
     *
     * @param input 输入的数值字符串
     * @return 转换后的结果字符串
     * @throws Exception 当输入为空或格式不正确时抛出异常
     */
    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        NumBaseEnum fromBase = (NumBaseEnum) fromBaseCombo.getSelectedItem();
        NumBaseEnum toBase = (NumBaseEnum) toBaseCombo.getSelectedItem();
        if (Objects.isNull(fromBase) || Objects.isNull(toBase) || Objects.equals(fromBase, toBase)) {
            return getExample();
        }

        String valueWithoutPrefix = input.replaceAll("^0[xXbBoO]", "");

        try {
            long decimalValue = parseToDecimal(valueWithoutPrefix, fromBase.getValue());
            return convertFromDecimal(decimalValue, toBase.getValue());
        } catch (NumberFormatException e) {
            throw new Exception("输入的数值格式不正确，请检查");
        }
    }

    /**
     * 将指定进制的字符串转换为十进制数值
     *
     * @param value 要转换的数值字符串
     * @param base  数值的进制
     * @return 转换后的十进制数值
     * @throws NumberFormatException 当输入包含无效字符时抛出
     */
    private long parseToDecimal(String value, int base) throws NumberFormatException {
        String upperValue = value.toUpperCase();
        long result = 0;

        for (int i = 0; i < upperValue.length(); i++) {
            char c = upperValue.charAt(i);
            int digit = NumBaseEnum.CHARS.indexOf(c);
            if (digit < 0 || digit >= base) {
                throw new NumberFormatException("无效的数字字符: " + c);
            }
            result = result * base + digit;
        }
        return result;
    }

    /**
     * 将十进制数值转换为指定进制的字符串
     *
     * @param value 要转换的十进制数值
     * @param base  目标进制
     * @return 转换后的字符串
     */
    private String convertFromDecimal(long value, int base) {
        if (value == 0) {
            return "0";
        }

        boolean negative = value < 0;
        long absValue = Math.abs(value);

        StringBuilder sb = new StringBuilder();
        while (absValue > 0) {
            int digit = (int) (absValue % base);
            sb.append(NumBaseEnum.CHARS.charAt(digit));
            absValue = absValue / base;
        }

        if (negative) {
            sb.append("-");
        }

        return sb.reverse().toString().toLowerCase();
    }

    /**
     * 获取示例输入值
     *
     * @return 示例值字符串
     */
    public String getExample() {
        return "255";
    }
}
