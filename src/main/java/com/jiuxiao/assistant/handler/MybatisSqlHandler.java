package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MybatisSqlHandler {

    private JRadioButton fillParamsRadio;
    private JRadioButton formatSqlRadio;

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fillParamsRadio = new JRadioButton("填充参数", true);
        formatSqlRadio = new JRadioButton("格式化SQL");
        ButtonGroup group = new ButtonGroup();
        group.add(fillParamsRadio);
        group.add(formatSqlRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setBackground(null);
        radioPanel.add(fillParamsRadio);
        radioPanel.add(formatSqlRadio);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(radioPanel, gbc);

        JLabel hintLabel = new JLabel("提示: 输入Mybatis日志中的SQL和参数");
        hintLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        panel.add(hintLabel, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        if (fillParamsRadio.isSelected()) {
            return fillParams(input);
        } else {
            return formatSql(input);
        }
    }

    private String fillParams(String input) throws Exception {
        String sql = null;
        String params = null;

        String[] lines = input.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Preparing:") || line.startsWith("Preparing:")) {
                int idx = line.indexOf(":");
                sql = line.substring(idx + 1).trim();
            } else if (line.startsWith("Parameters:") || line.startsWith("Parameters:")) {
                int idx = line.indexOf(":");
                params = line.substring(idx + 1).trim();
            }
        }

        if (sql == null || sql.isEmpty()) {
            throw new Exception("未找到SQL语句，请确保输入包含Preparing:");
        }

        if (params == null || params.isEmpty()) {
            return sql;
        }

        List<String> paramList = parseParams(params);
        int questionCount = countQuestionMarks(sql);

        if (paramList.size() != questionCount) {
            return "警告: 参数数量(" + paramList.size() + ")与占位符数量(" + questionCount + ")不匹配\n\n" + sql;
        }

        for (String param : paramList) {
            String replacement = convertParam(param);
            int idx = sql.indexOf("?");
            if (idx >= 0) {
                sql = sql.substring(0, idx) + replacement + sql.substring(idx + 1);
            }
        }

        return sql;
    }

    private List<String> parseParams(String params) {
        List<String> result = new ArrayList<>();
        params = params.trim();

        int parenthesesCount = 0;
        int start = 0;

        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            if (c == '(') {
                parenthesesCount++;
            } else if (c == ')') {
                parenthesesCount--;
            } else if (c == ',' && parenthesesCount == 0) {
                result.add(params.substring(start, i).trim());
                start = i + 1;
            }
        }

        if (start < params.length()) {
            result.add(params.substring(start).trim());
        }

        return result;
    }

    private String convertParam(String param) {
        param = param.trim();

        if (param.equals("null")) {
            return "null";
        }

        if (param.contains("(String)") || param.contains("(varchar)") || param.contains("(text)")
                || param.contains("(String)")) {
            int start = param.lastIndexOf("(");
            int end = param.lastIndexOf(")");
            if (start >= 0 && end > start) {
                String value = param.substring(0, start).trim();
                return "'" + value + "'";
            }
        }

        if (param.contains("(Integer)") || param.contains("(int)")) {
            int start = param.lastIndexOf("(");
            int end = param.lastIndexOf(")");
            if (start >= 0 && end > start) {
                return param.substring(0, start).trim();
            }
        }

        if (param.contains("(Long)") || param.contains("(long)")) {
            int start = param.lastIndexOf("(");
            int end = param.lastIndexOf(")");
            if (start >= 0 && end > start) {
                return param.substring(0, start).trim();
            }
        }

        if (param.contains("(Double)") || param.contains("(double)")) {
            int start = param.lastIndexOf("(");
            int end = param.lastIndexOf(")");
            if (start >= 0 && end > start) {
                return param.substring(0, start).trim();
            }
        }

        if (param.contains("(BigDecimal)")) {
            int start = param.lastIndexOf("(");
            int end = param.lastIndexOf(")");
            if (start >= 0 && end > start) {
                return param.substring(0, start).trim();
            }
        }

        if (param.contains("(Date)")) {
            int start = param.lastIndexOf("(");
            int end = param.lastIndexOf(")");
            if (start >= 0 && end > start) {
                String value = param.substring(0, start).trim();
                return "'" + value + "'";
            }
        }

        if (param.contains("(Timestamp)")) {
            int start = param.lastIndexOf("(");
            int end = param.lastIndexOf(")");
            if (start >= 0 && end > start) {
                String value = param.substring(0, start).trim();
                return "'" + value + "'";
            }
        }

        if (param.contains("(Boolean)") || param.contains("(boolean)")) {
            int start = param.lastIndexOf("(");
            int end = param.lastIndexOf(")");
            if (start >= 0 && end > start) {
                return param.substring(0, start).trim();
            }
        }

        return "'" + param + "'";
    }

    private int countQuestionMarks(String sql) {
        int count = 0;
        for (char c : sql.toCharArray()) {
            if (c == '?') {
                count++;
            }
        }
        return count;
    }

    private String formatSql(String input) {
        String original = input.trim();

        original = original.replaceAll("(?i)\\bSELECT\\b", "\nSELECT");
        original = original.replaceAll("(?i)\\bFROM\\b", "\nFROM");
        original = original.replaceAll("(?i)\\bWHERE\\b", "\nWHERE");
        original = original.replaceAll("(?i)\\bAND\\b", "\n  AND");
        original = original.replaceAll("(?i)\\bOR\\b", "\n  OR");
        original = original.replaceAll("(?i)\\bJOIN\\b", "\nJOIN");
        original = original.replaceAll("(?i)\\bLEFT JOIN\\b", "\nLEFT JOIN");
        original = original.replaceAll("(?i)\\bRIGHT JOIN\\b", "\nRIGHT JOIN");
        original = original.replaceAll("(?i)\\bINNER JOIN\\b", "\nINNER JOIN");
        original = original.replaceAll("(?i)\\bON\\b", "\n  ON");
        original = original.replaceAll("(?i)\\bGROUP BY\\b", "\nGROUP BY");
        original = original.replaceAll("(?i)\\bORDER BY\\b", "\nORDER BY");
        original = original.replaceAll("(?i)\\bLIMIT\\b", "\nLIMIT");

        original = original.replaceAll("\\s+", " ");
        original = original.replaceAll("\n ", "\n");

        return original.trim();
    }

    public String getExample() {
        if (fillParamsRadio.isSelected()) {
            return "Preparing: SELECT * FROM user WHERE id = ? and user = ?\nParameters: 1(Integer), jack(String)";
        } else {
            return "select * from user where id = 1 and name = 'test'";
        }
    }
}
