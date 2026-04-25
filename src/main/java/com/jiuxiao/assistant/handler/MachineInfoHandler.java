package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.NetworkInterfaceTypeEnum;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * MachineInfoHandler类负责处理和显示机器信息
 * 包括主机名、主机地址以及所有网络接口的详细信息
 *
 * @author 悟道九霄
 * @date 2026/4/25
 */
public class MachineInfoHandler {

    private JTextArea resultArea;

    /**
     * 创建并返回一个配置好的JPanel面板
     *
     * @return 配置好的JPanel面板，包含刷新按钮
     */
    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshInfo());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(refreshButton, gbc);

        return panel;
    }

    /**
     * 执行获取机器信息的操作
     *
     * @param input 输入参数（本实现中未使用）
     * @return 机器信息字符串
     * @throws Exception 可能抛出的异常
     */
    public String execute(String input) throws Exception {
        return getMachineInfo();
    }

    /**
     * 刷新显示机器信息
     */
    private void refreshInfo() {
        try {
            String info = getMachineInfo();
            resultArea.setText(info);
        } catch (Exception e) {
            resultArea.setText("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取机器的详细信息
     * 包括主机名、主机地址和所有网络接口信息
     *
     * @return 包含机器详细信息的字符串
     */
    private String getMachineInfo() {
        StringBuilder sb = new StringBuilder();

        try {
            InetAddress localhost = InetAddress.getLocalHost();
            sb.append("主机名: ").append(localhost.getHostName()).append("\n");
            sb.append("主机地址: ").append(localhost.getHostAddress()).append("\n");
        } catch (UnknownHostException e) {
            sb.append("主机名: 未知\n");
            sb.append("主机地址: 未知\n");
        }

        sb.append("\n--- 所有网络接口 ---\n");

        try {
            // 获取所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                // 跳过回环接口或未启用的接口
                if (NetworkInterfaceTypeEnum.shouldSkipLoopback(ni.isLoopback()) || !ni.isUp()) {
                    continue;
                }

                String name = ni.getDisplayName();
                // 根据枚举类型跳过特定接口
                if (NetworkInterfaceTypeEnum.shouldSkip(name)) {
                    continue;
                }

                sb.append("接口: ").append(name).append("\n");

                // 获取接口的所有IPv4地址
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof java.net.Inet4Address) {
                        sb.append("  IPv4: ").append(inetAddress.getHostAddress()).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            sb.append("获取网络接口失败: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 获取使用示例
     *
     * @return 空字符串（本实现中未提供示例）
     */

    public String getExample() {
        return "";
    }
}
