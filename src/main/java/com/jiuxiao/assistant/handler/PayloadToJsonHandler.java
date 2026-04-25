package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.PayloadFormatEnum;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PayloadToJsonHandler 类负责处理键值对与JSON格式之间的相互转换
 * 提供了用户界面和转换逻辑的实现
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
public class PayloadToJsonHandler {

    private JComboBox<PayloadFormatEnum> typeCombo;

    /**
     * 创建并返回一个配置面板
     *
     * @return 配置面板，包含转换类型选择和提示信息
     */
    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("转换类型:"), gbc);

        typeCombo = new ComboBox<>(PayloadFormatEnum.values());
        typeCombo.setSelectedItem(PayloadFormatEnum.KEY_VALUE_TO_JSON);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        JLabel hintLabel = new JLabel("提示: 有冒号/等号用标准格式，否则用交替格式");
        hintLabel.setForeground(JBColor.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(hintLabel, gbc);

        return panel;
    }

    /**
     * 执行转换操作
     *
     * @param input 输入字符串
     * @return 转换后的字符串
     * @throws Exception 当输入无效或转换失败时抛出异常
     */
    public String execute(String input) throws Exception {
        PayloadFormatEnum selectedType = (PayloadFormatEnum) typeCombo.getSelectedItem();
        if (Objects.isNull(selectedType)) return getExample();

        if (selectedType == PayloadFormatEnum.KEY_VALUE_TO_JSON) {
            return keyValueToJson(input);
        } else {
            return jsonToKeyValue(input);
        }
    }

    /**
     * 将键值对字符串转换为JSON格式
     *
     * @param input 输入的键值对字符串
     * @return JSON格式字符串
     * @throws Exception 当输入无效或转换失败时抛出异常
     */
    private String keyValueToJson(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        Map<String, String> map = new LinkedHashMap<>();
        String[] lines = input.trim().split("\n");

        String firstLine = lines[0];
        boolean isStandardFormat = firstLine.contains(":") || firstLine.contains("=");

        processFormatString(map, lines, isStandardFormat);

        if (map.isEmpty()) {
            throw new Exception("未识别到有效的键值对，请检查格式");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("  \"").append(escapeJson(entry.getKey())).append("\": \"")
                    .append(escapeJson(entry.getValue())).append("\"");
            if (++i < map.size()) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 处理格式字符串，根据标准格式或交替格式解析键值对
     *
     * @param map              用于存储解析结果的Map
     * @param lines            输入字符串的行数组
     * @param isStandardFormat 是否为标准格式（包含冒号或等号）
     */
    private static void processFormatString(Map<String, String> map, String[] lines, boolean isStandardFormat) {
        if (isStandardFormat) {
            // 使用正则表达式匹配标准格式（键:值 或 键=值）
            Pattern pattern = Pattern.compile("^([^:=]+)[:=](.*)$");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String key = matcher.group(1).trim();
                    String value = matcher.group(2).trim();
                    map.put(key, value);
                }
            }
        } else {
            // 处理交替格式（键值交替出现）
            for (int i = 0; i < lines.length; i++) {
                String key = lines[i].trim();
                if (key.isEmpty()) {
                    continue;
                }
                if (i + 1 < lines.length) {
                    String value = lines[i + 1].trim();
                    map.put(key, value);
                    i++;
                }
            }
        }
    }

    /**
     * 将JSON格式字符串转换为键值对格式
     *
     * @param input 输入的JSON格式字符串
     * @return 键值对格式字符串
     * @throws Exception 当输入无效或转换失败时抛出异常
     */
    private String jsonToKeyValue(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        if (!input.startsWith("{") || !input.endsWith("}")) {
            throw new Exception("JSON格式不正确，需要以{开头，以}结尾");
        }

        StringBuilder sb = new StringBuilder();
        String content = input.substring(1, input.length() - 1).trim();

        if (!content.isEmpty()) {
            // 使用正则表达式匹配JSON键值对
            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(content);

            boolean first = true;
            while (matcher.find()) {
                if (!first) {
                    sb.append("\n");
                }
                String key = matcher.group(1);
                String value = matcher.group(2);
                sb.append(key).append(": ").append(value);
                first = false;
            }

            if (first) {
                throw new Exception("JSON格式不正确，无法解析键值对");
            }
        }

        return sb.toString();
    }

    /**
     * 对字符串进行JSON转义处理
     *
     * @param str 需要转义的字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 根据当前选择的转换类型返回示例
     *
     * @return 示例字符串
     */
    public String getExample() {
        PayloadFormatEnum selectedType = (PayloadFormatEnum) typeCombo.getSelectedItem();
        if (selectedType == PayloadFormatEnum.KEY_VALUE_TO_JSON) {
            return "e\nig8eux599M=\nplatform\npc\ndeadline\n1777047184\ntrid\ncf937043d91744a9827";
        } else {
            return "{\n  \"User-Agent\": \"Mozilla/5.0\",\n  \"Content-Type\": \"application/json\",\n  \"Accept\": \"*/*\"\n}";
        }
    }
}
