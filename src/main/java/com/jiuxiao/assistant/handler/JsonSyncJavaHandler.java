package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.JsonJavaTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonToJavaHandler类处理JSON和Java代码之间的相互转换
 * 提供了界面创建和转换逻辑实现
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
public class JsonSyncJavaHandler {

    private JComboBox<JsonJavaTypeEnum> typeCombo;
    private JCheckBox useLombokCheckBox;
    private JCheckBox useAnnotationsCheckBox;

    /**
     * 创建并返回一个包含转换选项的面板
     *
     * @return 配置好的JPanel面板
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

        typeCombo = new ComboBox<>(JsonJavaTypeEnum.values());
        typeCombo.setSelectedItem(JsonJavaTypeEnum.JSON_TO_JAVA);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        useLombokCheckBox = new JCheckBox("使用Lombok注解", true);
        useAnnotationsCheckBox = new JCheckBox("生成Jackson注解", false);
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        checkPanel.setBackground(null);
        checkPanel.add(useLombokCheckBox);
        checkPanel.add(useAnnotationsCheckBox);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(checkPanel, gbc);

        return panel;
    }

    /**
     * 根据选择的类型执行转换
     *
     * @param input 输入的字符串
     * @return 转换后的结果
     * @throws Exception 当输入为空或格式不正确时抛出异常
     */
    public String execute(String input) throws Exception {
        JsonJavaTypeEnum selectedType = (JsonJavaTypeEnum) typeCombo.getSelectedItem();
        if (Objects.isNull(selectedType)) return getExample();

        if (selectedType == JsonJavaTypeEnum.JSON_TO_JAVA) {
            return jsonToJava(input);
        } else {
            return javaToJson(input);
        }
    }

    /**
     * 将JSON字符串转换为Java类代码
     *
     * @param input JSON字符串
     * @return 生成的Java类代码
     * @throws Exception 当输入为空或格式不正确时抛出异常
     */
    private String jsonToJava(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        input = input.trim();
        if (!input.startsWith("{")) {
            throw new Exception("JSON格式不正确，需要以{开头");
        }

        String className = "Root";
        Pattern classPattern = Pattern.compile("\"(\\w+)\"\\s*:");
        Matcher classMatcher = classPattern.matcher(input);
        if (classMatcher.find()) {
            String firstKey = classMatcher.group(1);
            className = capitalize(toCamelCase(firstKey));
        }

        StringBuilder sb = new StringBuilder();

        if (useLombokCheckBox.isSelected()) {
            sb.append("@Data\n");
            sb.append("@NoArgsConstructor\n");
            sb.append("@AllArgsConstructor\n");
        }

        sb.append("public class ").append(className).append(" {\n\n");

        Pattern pattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"[^\"]*\"|\\d+|true|false|null)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            String fieldName = toCamelCase(key);
            String type = inferType(value);

            if (useAnnotationsCheckBox.isSelected()) {
                sb.append("    @JsonProperty(\"").append(key).append("\")\n");
            }

            sb.append("    private ").append(type).append(" ").append(fieldName).append(";\n\n");
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * 将Java类代码转换为JSON字符串
     *
     * @param input Java类代码
     * @return 生成的JSON字符串
     * @throws Exception 当输入为空或格式不正确时抛出异常
     */
    private String javaToJson(String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            throw new Exception("输入不能为空");
        }

        Pattern fieldPattern = Pattern.compile("(private|public|protected)\\s+(\\w+)\\s+(\\w+)\\s*;");
        Matcher matcher = fieldPattern.matcher(input);

        if (!matcher.find()) {
            throw new Exception("未找到有效的Java字段声明");
        }

        matcher.reset();

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        boolean first = true;
        while (matcher.find()) {
            String type = matcher.group(2);
            String fieldName = matcher.group(3);

            if (!first) {
                sb.append(",\n");
            }

            String jsonKey = toSnakeCase(fieldName);
            String jsonValue = getDefaultValue(type);

            sb.append("  \"").append(jsonKey).append("\": ").append(jsonValue);
            first = false;
        }

        sb.append("\n}");
        return sb.toString();
    }

    /**
     * 根据JSON值推断对应的Java类型
     *
     * @param value JSON值
     * @return 对应的Java类型
     */
    private String inferType(String value) {
        if (value.startsWith("\"")) {
            return "String";
        } else if (value.equals("true") || value.equals("false")) {
            return "boolean";
        } else if (value.equals("null")) {
            return "Object";
        } else {
            if (value.contains(".")) {
                return "double";
            }
            return "int";
        }
    }

    /**
     * 根据Java类型获取对应的默认值
     *
     * @param type Java类型
     * @return 默认值字符串
     */
    private String getDefaultValue(String type) {
        switch (type.toLowerCase()) {
            case "string":
                return "\"\"";
            case "int":
            case "integer":
                return "0";
            case "long":
                return "0L";
            case "double":
            case "float":
                return "0.0";
            case "boolean":
                return "false";
            case "list":
            case "arraylist":
                return "new ArrayList<>()";
            case "map":
            case "hashmap":
                return "new HashMap<>()";
            default:
                return "null";
        }
    }

    /**
     * 将字符串转换为驼峰命名法
     *
     * @param str 输入字符串
     * @return 转换后的驼峰命名字符串
     */
    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : str.toCharArray()) {
            if (c == '_' || c == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串转换为下划线命名法
     *
     * @param str 输入字符串
     * @return 转换后的下划线命名字符串
     */
    private String toSnakeCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
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
     * 根据选择的转换类型返回示例
     *
     * @return 示例字符串
     */
    public String getExample() {
        JsonJavaTypeEnum selectedType = (JsonJavaTypeEnum) typeCombo.getSelectedItem();
        if (selectedType == JsonJavaTypeEnum.JSON_TO_JAVA) {
            return "{\n  \"user_name\": \"张三\",\n  \"user_age\": 25,\n  \"is_active\": true\n}";
        } else {
            return "private String userName;\nprivate Integer userAge;\nprivate boolean isActive;";
        }
    }
}
