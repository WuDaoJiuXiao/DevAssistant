package com.jiuxiao.assistant.handler;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MachineInfoHandler {

    private JButton refreshButton;
    private JLabel ipLabel;
    private JLabel hostnameLabel;
    private JTextArea resultArea;

    public JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshInfo());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(refreshButton, gbc);

        return panel;
    }

    public String execute(String input) throws Exception {
        return getMachineInfo();
    }

    private void refreshInfo() {
        try {
            String info = getMachineInfo();
            resultArea.setText(info);
        } catch (Exception e) {
            resultArea.setText("获取失败: " + e.getMessage());
        }
    }

    private String getMachineInfo() throws Exception {
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
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }

                String name = ni.getDisplayName();
                if (name.contains("VMware") || name.contains("VirtualBox") || name.contains("Docker")) {
                    continue;
                }

                sb.append("接口: ").append(name).append("\n");

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address) {
                        sb.append("  IPv4: ").append(addr.getHostAddress()).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            sb.append("获取网络接口失败: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    public String getExample() {
        return "";
    }
}
