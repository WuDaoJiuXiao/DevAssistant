package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayloadToJsonHandler {

    private JRadioButton keyValueToJsonRadio;
    private JRadioButton jsonToKeyValueRadio;

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        keyValueToJsonRadio = new JRadioButton("键值对 to JSON", true);
        jsonToKeyValueRadio = new JRadioButton("JSON to 键值对");
        ButtonGroup group = new ButtonGroup();
        group.add(keyValueToJsonRadio);
        group.add(jsonToKeyValueRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setBackground(null);
        radioPanel.add(keyValueToJsonRadio);
        radioPanel.add(jsonToKeyValueRadio);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(radioPanel, gbc);

        JLabel hintLabel = new JLabel("提示: 有冒号/等号用标准格式，否则用交替格式");
        hintLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        panel.add(hintLabel, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        if (keyValueToJsonRadio.isSelected()) {
            return keyValueToJson(input);
        } else {
            return jsonToKeyValue(input);
        }
    }

    private String keyValueToJson(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        Map<String, String> map = new LinkedHashMap<>();
        String[] lines = input.trim().split("\n");

        String firstLine = lines[0];
        boolean isStandardFormat = firstLine.contains(":") || firstLine.contains("=");

        if (isStandardFormat) {
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

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    public String getExample() {
        if (keyValueToJsonRadio.isSelected()) {
            return "e\nig8eux599M=\nplatform\npc\ndeadline\n1777047184\ntrid\ncf937043d91744a9827e65d5d3b158bu";
        } else {
            return "{\n  \"User-Agent\": \"Mozilla/5.0\",\n  \"Content-Type\": \"application/json\",\n  \"Accept\": \"*/*\"\n}";
        }
    }
}
