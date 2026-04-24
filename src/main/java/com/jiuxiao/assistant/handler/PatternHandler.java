package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PatternHandler {

    private JComboBox<String> patternCombo;

    private static final String[] PATTERNS = {
        "手机号码",
        "邮箱",
        "身份证号",
        "IP地址",
        "URL",
        "日期格式(yyyy-MM-dd)",
        "日期格式(yyyy/MM/dd)",
        "日期格式(yyyy.MM.dd)",
        "日期格式(yyyyMMdd)",
        "时间格式(HH:mm:ss)",
        "日期时间格式",
        "中文",
        "数字",
        "字母",
        "字母数字组合",
        "邮编",
        "银行卡号"
    };

    private static final Map<String, String> PATTERN_MAP = new LinkedHashMap<>();

    static {
        PATTERN_MAP.put("手机号码", "^1[3-9]\\d{9}$");
        PATTERN_MAP.put("邮箱", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        PATTERN_MAP.put("身份证号", "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dxX]$");
        PATTERN_MAP.put("IP地址", "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
        PATTERN_MAP.put("URL", "^https?://[\\w.-]+(:\\d+)?(/[\\w./-]*)?$");
        PATTERN_MAP.put("日期格式(yyyy-MM-dd)", "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");
        PATTERN_MAP.put("日期格式(yyyy/MM/dd)", "^\\d{4}/(0[1-9]|1[0-2])/(0[1-9]|[12]\\d|3[01])$");
        PATTERN_MAP.put("日期格式(yyyy.MM.dd)", "^\\d{4}.(0[1-9]|1[0-2]).(0[1-9]|[12]\\d|3[01])$");
        PATTERN_MAP.put("日期格式(yyyyMMdd)", "^\\d{8}$");
        PATTERN_MAP.put("时间格式(HH:mm:ss)", "^(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$");
        PATTERN_MAP.put("日期时间格式", "^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}$");
        PATTERN_MAP.put("中文", "^[\\u4e00-\\u9fa5]+$");
        PATTERN_MAP.put("数字", "^\\d+$");
        PATTERN_MAP.put("字母", "^[a-zA-Z]+$");
        PATTERN_MAP.put("字母数字组合", "^[a-zA-Z0-9]+$");
        PATTERN_MAP.put("邮编", "^\\d{6}$");
        PATTERN_MAP.put("银行卡号", "^\\d{16,19}$");
    }

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("选择正则:"), gbc);

        patternCombo = new JComboBox<>(PATTERNS);
        patternCombo.setSelectedIndex(0);
        gbc.gridx = 1;
        panel.add(patternCombo, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        String selectedPattern = (String) patternCombo.getSelectedItem();
        String regex = PATTERN_MAP.get(selectedPattern);

        if (input == null || input.trim().isEmpty()) {
            return "正则表达式: " + regex;
        }

        input = input.trim();
        boolean matches = input.matches(regex);

        return "正则表达式: " + regex + "\n" +
               "验证结果: " + (matches ? "✅ 匹配" : "❌ 不匹配");
    }

    public String getExample() {
        return "";
    }
}
