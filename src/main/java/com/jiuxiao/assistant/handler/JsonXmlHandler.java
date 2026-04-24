package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonXmlHandler {

    private JRadioButton jsonToXmlRadio;
    private JRadioButton xmlToJsonRadio;
    private JTextField rootElementField;

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        jsonToXmlRadio = new JRadioButton("JSON to XML", true);
        xmlToJsonRadio = new JRadioButton("XML to JSON");
        ButtonGroup group = new ButtonGroup();
        group.add(jsonToXmlRadio);
        group.add(xmlToJsonRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setBackground(null);
        radioPanel.add(jsonToXmlRadio);
        radioPanel.add(xmlToJsonRadio);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(radioPanel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("根元素:"), gbc);

        rootElementField = new JTextField("root", 15);
        gbc.gridx = 1;
        panel.add(rootElementField, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        if (jsonToXmlRadio.isSelected()) {
            return jsonToXml(input);
        } else {
            return xmlToJson(input);
        }
    }

    private String jsonToXml(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        if (!input.startsWith("{")) {
            throw new Exception("JSON格式不正确，需要以{开头");
        }

        String rootElement = rootElementField.getText().trim();
        if (rootElement.isEmpty()) {
            rootElement = "root";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<").append(rootElement).append(">\n");

        parseJsonToXml(input, sb, 1);

        sb.append("</").append(rootElement).append(">");

        return sb.toString();
    }

    private void parseJsonToXml(String json, StringBuilder sb, int indent) throws Exception {
        json = json.trim();

        if (json.startsWith("{")) {
            json = json.substring(1);
            if (json.endsWith("}")) {
                json = json.substring(0, json.length() - 1);
            }
        }

        Pattern pattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"[^\"]*\"|\\d+|true|false|null|\\[.*?\\]|\\{.*?\\})");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);

            String indentStr = getIndent(indent);
            sb.append(indentStr).append("<").append(key).append(">");

            if (value.startsWith("[")) {
                sb.append("\n");
                parseJsonArrayToXml(value, sb, indent + 1);
                sb.append(indentStr).append("</").append(key).append(">\n");
            } else if (value.startsWith("{")) {
                sb.append("\n");
                parseJsonToXml(value, sb, indent + 1);
                sb.append(indentStr).append("</").append(key).append(">\n");
            } else if (value.startsWith("\"")) {
                sb.append(escapeXml(value.substring(1, value.length() - 1))).append("</").append(key).append(">\n");
            } else if (value.equals("null")) {
                sb.append("</").append(key).append(">\n");
            } else {
                sb.append(value).append("</").append(key).append(">\n");
            }
        }
    }

    private void parseJsonArrayToXml(String arrayJson, StringBuilder sb, int indent) throws Exception {
        if (arrayJson.startsWith("[")) {
            arrayJson = arrayJson.substring(1);
            if (arrayJson.endsWith("]")) {
                arrayJson = arrayJson.substring(0, arrayJson.length() - 1);
            }
        }

        String[] items = splitJsonArray(arrayJson);
        String indentStr = getIndent(indent);

        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) {
                continue;
            }

            if (item.startsWith("{")) {
                sb.append(indentStr).append("<item>\n");
                parseJsonToXml(item, sb, indent + 1);
                sb.append(indentStr).append("</item>\n");
            } else if (item.startsWith("\"")) {
                sb.append(indentStr).append("<item>")
                  .append(escapeXml(item.substring(1, item.length() - 1)))
                  .append("</item>\n");
            } else {
                sb.append(indentStr).append("<item>").append(item).append("</item>\n");
            }
        }
    }

    private String[] splitJsonArray(String arrayJson) {
        int bracketCount = 0;
        int start = 0;
        java.util.List<String> result = new java.util.ArrayList<>();

        for (int i = 0; i < arrayJson.length(); i++) {
            char c = arrayJson.charAt(i);
            if (c == '[' || c == '{') {
                bracketCount++;
            } else if (c == ']' || c == '}') {
                bracketCount--;
            } else if (c == ',' && bracketCount == 0) {
                result.add(arrayJson.substring(start, i).trim());
                start = i + 1;
            }
        }

        if (start < arrayJson.length()) {
            result.add(arrayJson.substring(start).trim());
        }

        return result.toArray(new String[0]);
    }

    private String xmlToJson(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        if (!input.startsWith("<")) {
            throw new Exception("XML格式不正确，需要以<开头");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        Pattern xmlPattern = Pattern.compile("<(\\w+)>([^<]*)</\\1>");
        Matcher matcher = xmlPattern.matcher(input);

        boolean first = true;
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();

            if (!first) {
                sb.append(",\n");
            }

            if (value.isEmpty()) {
                sb.append("  \"").append(key).append("\": null");
            } else if (isNumeric(value)) {
                sb.append("  \"").append(key).append("\": ").append(value);
            } else if (value.equals("true") || value.equals("false")) {
                sb.append("  \"").append(key).append("\": ").append(value);
            } else {
                sb.append("  \"").append(key).append("\": \"").append(unescapeXml(value)).append("\"");
            }
            first = false;
        }

        sb.append("\n}");
        return sb.toString();
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    private String escapeXml(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }

    private String unescapeXml(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&quot;", "\"")
                  .replace("&apos;", "'")
                  .replace("&amp;", "&");
    }

    public String getExample() {
        if (jsonToXmlRadio.isSelected()) {
            return "{\n  \"name\": \"张三\",\n  \"age\": 25\n}";
        } else {
            return "<root>\n  <name>张三</name>\n  <age>25</age>\n</root>";
        }
    }
}
