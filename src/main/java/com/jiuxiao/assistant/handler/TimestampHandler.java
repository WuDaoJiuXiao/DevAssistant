package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 提供时间戳与日期之间的相互转换功能
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public class TimestampHandler {

    private JRadioButton timestampToDateRadio;

    private JTextField dateFormatField;

    /**
     * 创建时间戳转换功能的界面面板
     * 使用GridBagLayout进行布局管理
     * 包含转换类型选择和日期格式输入
     *
     * @return 配置好的JPanel面板
     */
    public JPanel createPanel() {
        // 创建主面板，使用GridBagLayout布局
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);  // 设置组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL;  // 水平填充

        // 转换类型选择
        timestampToDateRadio = new JRadioButton("时间戳 to 日期", true);
        JRadioButton dateToTimestampRadio = new JRadioButton("日期 to 时间戳");
        ButtonGroup group = new ButtonGroup();
        group.add(timestampToDateRadio);
        group.add(dateToTimestampRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setBackground(null);
        radioPanel.add(timestampToDateRadio);
        radioPanel.add(dateToTimestampRadio);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(radioPanel, gbc);

        // 日期格式
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("日期格式:"), gbc);

        dateFormatField = new JTextField("yyyy-MM-dd HH:mm:ss", 20);
        gbc.gridx = 1;
        panel.add(dateFormatField, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        if (timestampToDateRadio.isSelected()) {
            // 时间戳转日期
            long timestamp = Long.parseLong(input.trim());
            String format = dateFormatField.getText().trim();
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(new Date(timestamp));
        } else {
            // 日期转时间戳
            String format = dateFormatField.getText().trim();
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = sdf.parse(input.trim());
            return String.valueOf(date.getTime());
        }
    }

    public String getExample() {
        if (timestampToDateRadio.isSelected()) {
            return String.valueOf(System.currentTimeMillis());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatField.getText().trim());
            return sdf.format(new Date());
        }
    }
}