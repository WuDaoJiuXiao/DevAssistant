package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.JsonXmlTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonSyncXmlHandler 类用于处理 JSON 和 XML 之间的相互转换
 * 提供了创建转换面板、执行转换以及各种辅助方法
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
public class JsonSyncXmlHandler {

    private JComboBox<JsonXmlTypeEnum> typeCombo;
    private JTextField rootElementField;

    /**
     * 创建并返回一个包含转换类型和根元素设置的 JPanel
     *
     * @return 配置好的 JPanel
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

        typeCombo = new ComboBox<>(JsonXmlTypeEnum.values());
        typeCombo.setSelectedItem(JsonXmlTypeEnum.JSON_TO_XML);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("根元素:"), gbc);

        rootElementField = new JTextField("root", 15);
        gbc.gridx = 1;
        panel.add(rootElementField, gbc);

        return panel;
    }

    /**
     * 执行 JSON 和 XML 之间的转换
     *
     * @param input 输入的字符串
     * @return 转换后的字符串
     * @throws Exception 当输入为空或格式不正确时抛出异常
     */
    public String execute(String input) throws Exception {
        JsonXmlTypeEnum selectedType = (JsonXmlTypeEnum) typeCombo.getSelectedItem();
        if (Objects.isNull(selectedType)) return getExample();

        if (selectedType == JsonXmlTypeEnum.JSON_TO_XML) {
            return jsonToXml(input);
        } else {
            return xmlToJson(input);
        }
    }

    /**
     * 将 JSON 字符串转换为 XML 格式
     *
     * @param input 输入的 JSON 字符串
     * @return 转换后的 XML 字符串
     * @throws Exception 当输入为空或格式不正确时抛出异常
     */
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

    /**
     * 递归解析 JSON 对象并转换为 XML 格式
     *
     * @param json   输入的 JSON 字符串
     * @param sb     用于构建 XML 的 StringBuilder
     * @param indent 当前的缩进级别
     * @throws Exception 当 JSON 格式不正确时抛出异常
     */
    private void parseJsonToXml(String json, StringBuilder sb, int indent) throws Exception {
        json = json.trim();

        if (json.startsWith("{")) {
            json = json.substring(1);
            if (json.endsWith("}")) {
                json = json.substring(0, json.length() - 1);
            }
        }

        Pattern pattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"[^\"]*\"|\\d+|true|false|null|\\[.*?]|\\{.*?})");
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

    /**
     * 解析 JSON 数组并转换为 XML 格式
     *
     * @param arrayJson 输入的 JSON 数组字符串
     * @param sb        用于构建 XML 的 StringBuilder
     * @param indent    当前的缩进级别
     * @throws Exception 当 JSON 格式不正确时抛出异常
     */
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

    /**
     * 分割 JSON 数组字符串为单独的元素
     *
     * @param arrayJson 输入的 JSON 数组字符串
     * @return 分割后的字符串数组
     */
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

    /**
     * 将 XML 字符串转换为 JSON 格式
     *
     * @param input 输入的 XML 字符串
     * @return 转换后的 JSON 字符串
     * @throws Exception 当输入为空或格式不正确时抛出异常
     */
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

    /**
     * 检查字符串是否为数字
     *
     * @param str 要检查的字符串
     * @return 如果是数字返回 true，否则返回 false
     */
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

    /**
     * 获取指定级别的缩进字符串
     *
     * @param level 缩进级别
     * @return 缩进字符串
     */
    private String getIndent(int level) {
        return "  ".repeat(Math.max(0, level));
    }

    /**
     * 对字符串进行 XML 转义处理
     *
     * @param str 要转义的字符串
     * @return 转义后的字符串
     */
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

    /**
     * 对字符串进行 XML 反转义处理
     *
     * @param str 要反转义的字符串
     * @return 反转义后的字符串
     */
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

    /**
     * 根据当前选择的转换类型返回示例字符串
     *
     * @return 示例字符串
     */
    public String getExample() {
        JsonXmlTypeEnum selectedType = (JsonXmlTypeEnum) typeCombo.getSelectedItem();
        if (selectedType == JsonXmlTypeEnum.JSON_TO_XML) {
            return "{\n  \"name\": \"张三\",\n  \"age\": 25\n}";
        } else {
            return "<root>\n  <name>张三</name>\n  <age>25</age>\n</root>";
        }
    }
}
