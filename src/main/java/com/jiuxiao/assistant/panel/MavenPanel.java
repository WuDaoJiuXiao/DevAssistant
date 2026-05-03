package com.jiuxiao.assistant.panel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.MavenRepositoryEnum;
import com.jiuxiao.assistant.handler.MavenHandler;
import com.jiuxiao.assistant.util.SystemHandleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Maven依赖管理面板类
 * 用于搜索和管理Maven依赖，提供用户友好的界面
 * 主要功能包括：
 * 1. 搜索Maven依赖
 * 2. 显示搜索结果
 * 3. 复制依赖信息
 *
 * @author 悟道九霄
 * @date 2026/4/26
 */
public class MavenPanel extends JPanel {

    private final Project project;
    private boolean initialized = false;

    private JTextField artifactIdField;
    private JTextField versionField;
    private JComboBox<String> mirrorCombo;
    private JButton searchButton;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    private MavenHandler mavenHandler;
    private static final String[] COLUMN_NAMES = {"编号", "GroupId", "ArtifactId", "Version", "操作"};
    private static final int COPY_BUTTON_WIDTH = 60;

    public MavenPanel(Project project) {
        this.project = project;
        initUI();
        initHandler();
    }

    /**
     * 初始化用户界面
     */
    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(JBUI.Borders.empty(5));

        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        createTable();
    }

    /**
     * 创建搜索面板
     * 第一行：ArtifactId和Version搜索框
     * 第二行：镜像选择下拉框和搜索按钮
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(JBUI.Borders.empty(5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ========== 第一行：ArtifactId ==========
        // ArtifactId 标签（右对齐，占20%宽度）
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel artifactIdLabel = createLabel("ArtifactId:");
        searchPanel.add(artifactIdLabel, gbc);

        // ArtifactId 搜索框（占80%宽度）
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.anchor = GridBagConstraints.WEST;
        artifactIdField = createTextField();
        artifactIdField.setPreferredSize(new Dimension(0, 28));
        searchPanel.add(artifactIdField, gbc);

        // ========== 第二行：Version ==========
        // Version 标签（右对齐，占20%宽度）
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel versionLabel = createLabel("Version:");
        searchPanel.add(versionLabel, gbc);

        // Version 搜索框（占80%宽度）
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
        gbc.anchor = GridBagConstraints.WEST;
        versionField = createTextField();
        versionField.setPreferredSize(new Dimension(0, 28));
        searchPanel.add(versionField, gbc);

        // ========== 第三行：镜像下拉框和搜索按钮 ==========
        // 镜像标签（右对齐，占20%宽度）
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel mirrorLabel = createLabel("镜像:");
        searchPanel.add(mirrorLabel, gbc);

        // 镜像下拉框和搜索按钮容器（占80%宽度）
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.8;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        actionPanel.setOpaque(false);

        mirrorCombo = new ComboBox<>();
        mirrorCombo.setFont(new Font(null, Font.PLAIN, 11));
        mirrorCombo.setPreferredSize(new Dimension(150, 28));
        actionPanel.add(mirrorCombo);

        searchButton = new JButton("搜索");
        searchButton.setFont(new Font(null, Font.PLAIN, 11));
        searchButton.setPreferredSize(new Dimension(80, 28));
        searchButton.addActionListener(e -> performSearch());
        actionPanel.add(searchButton);

        searchPanel.add(actionPanel, gbc);

        return searchPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(null, Font.PLAIN, 11));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font(null, Font.PLAIN, 11));
        return field;
    }

    /**
     * 创建数据表格
     */
    private void createTable() {
        Vector<String> columnNameVector = new Vector<>(Arrays.asList(COLUMN_NAMES));

        // 创建表格数据模型，设置单元格不可编辑
        tableModel = new DefaultTableModel(new Vector<>(), columnNameVector) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // 只有"复制"列可点击
            }
        };

        dataTable = new JBTable(tableModel);
        dataTable.setFont(new Font(null, Font.PLAIN, 11));
        dataTable.getTableHeader().setFont(new Font(null, Font.BOLD, 11));
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setRowSelectionAllowed(true);
        dataTable.setColumnSelectionAllowed(false);

        // 设置复制列的渲染器和编辑器
        TableColumn copyColumn = dataTable.getColumnModel().getColumn(4);
        copyColumn.setCellRenderer(new ButtonRenderer());
        copyColumn.setCellEditor(new ButtonEditor());
        // 固定复制列宽度
        copyColumn.setPreferredWidth(COPY_BUTTON_WIDTH);
        copyColumn.setMinWidth(COPY_BUTTON_WIDTH);
        copyColumn.setMaxWidth(COPY_BUTTON_WIDTH);
        copyColumn.setWidth(COPY_BUTTON_WIDTH);

        // 设置其他列初始宽度
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        dataTable.getColumnModel().getColumn(0).setMinWidth(50);
        dataTable.getColumnModel().getColumn(0).setMaxWidth(50);

        dataTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        dataTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        dataTable.getColumnModel().getColumn(3).setMinWidth(100);

        // 监听面板大小变化，动态调整列宽（复制列除外）
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustColumnWidths();
            }
        });

        // 创建滚动面板
        JBScrollPane scrollPane = new JBScrollPane(dataTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 调整表格列宽（复制列固定宽度，其他列自适应）
     */
    private void adjustColumnWidths() {
        if (dataTable == null || dataTable.getParent() == null) return;

        int tableWidth = dataTable.getParent().getParent().getWidth();
        if (tableWidth <= 0) tableWidth = 800;

        // 固定列宽的总和
        int fixedWidth = 50 + COPY_BUTTON_WIDTH + 20; // 编号列 + 复制列 + 边距
        int remainingWidth = tableWidth - fixedWidth;

        // 动态分配宽度
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        dataTable.getColumnModel().getColumn(1).setPreferredWidth((int) (remainingWidth * 0.45)); // GroupId
        dataTable.getColumnModel().getColumn(2).setPreferredWidth((int) (remainingWidth * 0.35)); // ArtifactId
        dataTable.getColumnModel().getColumn(3).setPreferredWidth((int) (remainingWidth * 0.20)); // Version
    }

    /**
     * 初始化处理器
     */
    private void initHandler() {
        mavenHandler = new MavenHandler();
        initialized = true;

        SwingUtilities.invokeLater(() -> {
            List<String> mirrors = mavenHandler.getMirrorList();
            mirrorCombo.removeAllItems();
            for (String mirror : mirrors) {
                mirrorCombo.addItem(mirror);
            }
            mirrorCombo.setSelectedItem(MavenRepositoryEnum.MAVEN_CENTER.getDesc());
        });
    }

    /**
     * 执行搜索
     */
    private void performSearch() {
        if (!initialized) return;

        String artifactId = artifactIdField.getText();
        String version = versionField.getText();
        String mirror = (String) mirrorCombo.getSelectedItem();

        if (artifactId == null || artifactId.trim().isEmpty()) {
            SystemHandleUtil.showWarning(project, "请输入ArtifactId进行搜索");
            return;
        }

        searchButton.setEnabled(false);
        searchButton.setText("搜索中...");

        new SwingWorker<List<MavenHandler.MavenArtifact>, Void>() {
            @Override
            protected List<MavenHandler.MavenArtifact> doInBackground() {
                return mavenHandler.search(artifactId, version, mirror);
            }

            @Override
            protected void done() {
                try {
                    List<MavenHandler.MavenArtifact> results = get();
                    updateTableData(results);
                } catch (Exception e) {
                    e.printStackTrace();
                    SystemHandleUtil.showWarning(project, "搜索失败");
                } finally {
                    searchButton.setEnabled(true);
                    searchButton.setText("搜索");
                }
            }
        }.execute();
    }

    /**
     * 更新表格数据
     */
    private void updateTableData(List<MavenHandler.MavenArtifact> artifacts) {
        tableModel.setRowCount(0);
        appendTableData(artifacts);
        SwingUtilities.invokeLater(this::adjustColumnWidths);
    }

    /**
     * 追加表格数据
     */
    private void appendTableData(List<MavenHandler.MavenArtifact> artifacts) {
        int startIndex = tableModel.getRowCount();
        for (int i = 0; i < artifacts.size(); i++) {
            MavenHandler.MavenArtifact artifact = artifacts.get(i);
            Vector<Object> row = new Vector<>();
            row.add(startIndex + i + 1);
            row.add(artifact.getGroupId());
            row.add(artifact.getArtifactId());
            row.add(artifact.getVersion());
            row.add("复制");
            tableModel.addRow(row);
        }
    }

    /**
     * 复制依赖
     */
    private void copyDependency(MavenHandler.MavenArtifact artifact) {
        if (mavenHandler.copyToClipboard(artifact)) {
            SystemHandleUtil.showInformation(project, "复制成功");
        } else {
            SystemHandleUtil.showError(project, "复制失败");
        }
    }

    /**
     * 从行数据获取MavenArtifact对象
     */
    private MavenHandler.MavenArtifact getArtifactFromRow(int row) {
        List<MavenHandler.MavenArtifact> currentResults = mavenHandler.getCurrentResults();
        if (row >= 0 && row < currentResults.size()) {
            return currentResults.get(row);
        }
        return null;
    }

    /**
     * 按钮渲染器 - 用于显示"复制"按钮
     */
    private static class ButtonRenderer extends JLabel implements TableCellRenderer {
        public ButtonRenderer() {
            setText("复制");
            setFont(new Font(null, Font.PLAIN, 11));
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createLineBorder(JBColor.GRAY, 1));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "复制" : value.toString());
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(UIManager.getColor("Button.background"));
                setForeground(JBColor.BLACK);
            }
            return this;
        }
    }

    /**
     * 按钮编辑器 - 处理按钮点击事件
     */
    private class ButtonEditor extends DefaultCellEditor {
        private final JLabel label;
        private int clickedRow;

        public ButtonEditor() {
            super(new JCheckBox());
            label = new JLabel("复制", SwingConstants.CENTER);
            label.setFont(new Font(null, Font.PLAIN, 11));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createLineBorder(JBColor.GRAY, 1));

            // 添加鼠标点击监听
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    MavenHandler.MavenArtifact artifact = getArtifactFromRow(clickedRow);
                    if (artifact != null) {
                        copyDependency(artifact);
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            clickedRow = row;
            label.setText(value == null ? "复制" : value.toString());
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
            return label;
        }

        @Override
        public Object getCellEditorValue() {
            return "复制";
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }
}