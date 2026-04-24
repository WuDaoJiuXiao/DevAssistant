package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class BinaryHandler {

    private JComboBox<String> fromBaseCombo;
    private JComboBox<String> toBaseCombo;

    private static final String[] BASES = {"2 (二进制)", "8 (八进制)", "10 (十进制)", "16 (十六进制)", "36 (三十六进制)"};

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("源进制:"), gbc);

        fromBaseCombo = new JComboBox<>(BASES);
        fromBaseCombo.setSelectedIndex(3);
        gbc.gridx = 1;
        panel.add(fromBaseCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("目标进制:"), gbc);

        toBaseCombo = new JComboBox<>(BASES);
        toBaseCombo.setSelectedIndex(2);
        gbc.gridx = 1;
        panel.add(toBaseCombo, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();

        int fromBase = getBaseValue(fromBaseCombo.getSelectedIndex());
        int toBase = getBaseValue(toBaseCombo.getSelectedIndex());

        if (fromBase == toBase) {
            return input;
        }

        String valueWithoutPrefix = input.replaceAll("^0[xXbBoO]", "");

        try {
            long decimalValue = parseToDecimal(valueWithoutPrefix, fromBase);

            return convertFromDecimal(decimalValue, toBase);
        } catch (NumberFormatException e) {
            throw new Exception("输入的数值格式不正确，请检查");
        }
    }

    private int getBaseValue(int index) {
        switch (index) {
            case 0:
                return 2;
            case 1:
                return 8;
            case 2:
                return 10;
            case 3:
                return 16;
            case 4:
                return 36;
            default:
                return 10;
        }
    }

    private long parseToDecimal(String value, int base) throws NumberFormatException {
        String upperValue = value.toUpperCase();
        long result = 0;
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < upperValue.length(); i++) {
            char c = upperValue.charAt(i);
            int digit = chars.indexOf(c);
            if (digit < 0 || digit >= base) {
                throw new NumberFormatException("无效的数字字符: " + c);
            }
            result = result * base + digit;
        }
        return result;
    }

    private String convertFromDecimal(long value, int base) {
        if (value == 0) {
            return "0";
        }

        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        boolean negative = value < 0;
        long absValue = Math.abs(value);

        StringBuilder sb = new StringBuilder();
        while (absValue > 0) {
            int digit = (int) (absValue % base);
            sb.append(chars.charAt(digit));
            absValue = absValue / base;
        }

        if (negative) {
            sb.append("-");
        }

        return sb.reverse().toString().toLowerCase();
    }

    public String getExample() {
        return "255";
    }
}
