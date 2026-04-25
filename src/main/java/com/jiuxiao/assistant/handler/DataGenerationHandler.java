package com.jiuxiao.assistant.handler;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.DataGenTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.UUID;

/**
 * 数据生成处理器类，用于创建数据生成面板和处理数据生成逻辑
 * 该类提供了多种数据类型的生成功能，包括UUID、雪花ID、时间戳、随机数等
 * 通过界面组件让用户可以选择生成类型、数量和其他相关参数
 *
 * @author 悟道九霄
 * @date 2026/4/24
 */
public class DataGenerationHandler {

    private JComboBox<DataGenTypeEnum> dataTypeCombo;
    private JTextField countField;
    private JTextField lengthField;
    private JCheckBox includeSymbolsCheckBox;

    /**
     * 创建数据生成面板
     * 使用GridBagLayout布局管理器，添加了数据类型、生成数量、生成位数和包含符号等控件
     * 根据选择的数据类型动态调整控件的可用状态
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
        panel.add(new JLabel("数据类型:"), gbc);

        dataTypeCombo = new ComboBox<>(DataGenTypeEnum.values());
        dataTypeCombo.setSelectedItem(DataGenTypeEnum.UUID);
        dataTypeCombo.addActionListener(e -> updatePanelState());
        gbc.gridx = 1;
        panel.add(dataTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("生成数量:"), gbc);

        countField = new JTextField("1", 10);
        gbc.gridx = 1;
        panel.add(countField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("生成位数:"), gbc);

        lengthField = new JTextField("16", 10);
        gbc.gridx = 1;
        panel.add(lengthField, gbc);

        includeSymbolsCheckBox = new JCheckBox("包含常用符号", false);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(includeSymbolsCheckBox, gbc);

        updatePanelState();
        return panel;
    }

    /**
     * 根据选择的数据类型更新面板状态
     * 当选择随机字符串类型时，启用生成位数和包含符号控件
     * 其他类型时禁用这些控件
     */
    private void updatePanelState() {
        DataGenTypeEnum selected = (DataGenTypeEnum) dataTypeCombo.getSelectedItem();
        boolean showOptions = selected == DataGenTypeEnum.RANDOM_STRING;
        lengthField.setEnabled(showOptions);
        includeSymbolsCheckBox.setEnabled(showOptions);
    }

    /**
     * 执行数据生成
     * 根据用户选择的数据类型和参数生成相应的数据
     * 处理了生成数量的验证和默认值设置
     *
     * @param input 输入参数（当前未使用）
     * @return 生成的数据字符串，每条数据占一行
     * @throws Exception 可能抛出的异常
     */
    public String execute(String input) throws Exception {
        StringBuilder sb = new StringBuilder();
        DataGenTypeEnum selectedType = (DataGenTypeEnum) dataTypeCombo.getSelectedItem();
        if (Objects.isNull(selectedType)) return getExample();

        int count;
        try {
            String countText = countField.getText().trim();
            if (countText.isEmpty()) {
                count = 1;
            } else {
                count = Integer.parseInt(countText);
            }
            if (count <= 0 || count > 1000) {
                count = 1;
            }
        } catch (NumberFormatException e) {
            count = 1;
        }

        processSelectedType(sb, selectedType, count);
        return sb.toString();
    }

    /**
     * 根据选择的数据类型处理数据生成
     * 根据不同的数据类型调用相应的生成方法
     *
     * @param sb           用于存储生成结果的StringBuilder
     * @param selectedType 选中的数据类型
     * @param count        生成数量
     */
    private void processSelectedType(StringBuilder sb, DataGenTypeEnum selectedType, int count) {
        switch (selectedType) {
            case UUID:
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(UUID.randomUUID());
                }
                break;
            case SNOWFLAKE:
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(generateSnowflakeId());
                }
                break;
            case TIMESTAMP_MS:
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(System.currentTimeMillis());
                }
                break;
            case TIMESTAMP_SEC:
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(System.currentTimeMillis() / 1000);
                }
                break;
            case RANDOM_INT:
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append((int) (Math.random() * 100000));
                }
                break;
            case RANDOM_STRING:
                int length;
                try {
                    String lengthText = lengthField.getText().trim();
                    if (lengthText.isEmpty()) {
                        length = 16;
                    } else {
                        length = Integer.parseInt(lengthText);
                    }
                    if (length <= 0 || length > 200) {
                        length = 16;
                    }
                } catch (NumberFormatException e) {
                    length = 16;
                }

                boolean includeSymbols = includeSymbolsCheckBox.isSelected();
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(generateRandomString(length, includeSymbols));
                }
                break;
            case RANDOM_UUID_NO_DASH:
                for (int i = 0; i < count; i++) {
                    if (i > 0) {
                        sb.append("\n");
                    }
                    sb.append(UUID.randomUUID().toString().replace("-", ""));
                }
                break;
        }
    }

    /**
     * 生成雪花ID
     * 使用雪花算法生成唯一ID
     *
     * @return 雪花ID
     */
    private long generateSnowflakeId() {
        long timestamp = System.currentTimeMillis() - 1609459200000L;
        long workerId = 1L;
        long sequence = 0L;

        return (timestamp << 22) | (workerId << 12) | sequence;
    }

    /**
     * 生成随机字符串
     * 根据指定的长度和是否包含符号生成随机字符串
     *
     * @param length         字符串长度
     * @param includeSymbols 是否包含符号
     * @return 生成的随机字符串
     */
    private String generateRandomString(int length, boolean includeSymbols) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        if (includeSymbols) {
            chars += "!@#$%^&*()-_=+[]{}|;:,.<>?";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 获取示例
     * 当前未实现，返回空字符串
     *
     * @return 空字符串
     */
    public String getExample() {
        return "";
    }
}
