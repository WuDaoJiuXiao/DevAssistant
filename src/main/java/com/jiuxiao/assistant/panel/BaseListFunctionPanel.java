package com.jiuxiao.assistant.panel;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * 基础列表功能面板类，提供带有标签页、搜索和表格显示的基础功能
 * 继承此类可以快速实现具有列表展示功能的界面组件
 *
 * @author 悟道九霄
 * @date 2026/4/18
 */
public abstract class BaseListFunctionPanel extends JPanel {

    // 标签页组件
    protected JTabbedPane tabbedPane;
    // 存储每个标签页的搜索框
    protected Map<Integer, JTextField> tabSearchFields = new HashMap<>();
    // 存储每个标签页的表格
    protected Map<Integer, JTable> tabTables = new HashMap<>();
    // 存储每个标签页的表格数据模型
    protected Map<Integer, DefaultTableModel> tabTableModels = new HashMap<>();
    // 存储每个标签页的面板
    protected Map<Integer, JPanel> tabPanels = new HashMap<>();

    /**
     * 构造函数，初始化UI组件
     */
    public BaseListFunctionPanel() {
        // 设置UI组件的字体样式
        UIManager.put("Label.font", new Font(null, Font.PLAIN, 11));
        UIManager.put("Button.font", new Font(null, Font.PLAIN, 11));
        UIManager.put("TextField.font", new Font(null, Font.PLAIN, 11));
        UIManager.put("Table.font", new Font(null, Font.PLAIN, 11));
        initUI();
    }

    /**
     * 初始化UI界面
     */
    private void initUI() {
        // 设置布局和边距
        setLayout(new BorderLayout(10, 10));
        setBorder(JBUI.Borders.empty(10));

        // 创建标签页
        tabbedPane = new JBTabbedPane();
        String[] tabNames = getTabNames();

        // 遍历标签页名称数组，为每个标签页创建面板
        for (int i = 0; i < tabNames.length; i++) {
            JPanel tabPanel = createTabPanel(i);
            tabPanels.put(i, tabPanel);
            tabbedPane.addTab(tabNames[i], tabPanel);
        }

        // 添加标签页切换事件监听
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() >= 0) {
                onTabChanged(tabbedPane.getSelectedIndex());
            }
        });

        // 将标签页添加到面板中心
        add(tabbedPane, BorderLayout.CENTER);

        // 如果有标签页，默认选中第一个标签页
        if (tabNames.length > 0) {
            onTabChanged(0);
        }
    }

    /**
     * 创建单个标签页面板
     *
     * @param tabIndex 标签页索引
     * @return 创建的面板
     */
    protected JPanel createTabPanel(int tabIndex) {
        // 创建使用边界布局的面板
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 搜索面板
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(JBUI.Borders.empty(5));

        JTextField localSearchField = new JTextField();
        localSearchField.setFont(new Font(null, Font.PLAIN, 11));
        localSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch(tabIndex);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch(tabIndex);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch(tabIndex);
            }
        });
        tabSearchFields.put(tabIndex, localSearchField);

        JButton localSearchButton = new JButton("搜索");
        localSearchButton.setFont(new Font(null, Font.PLAIN, 11));
        localSearchButton.addActionListener(e -> performSearch(tabIndex));

        JButton localRefreshButton = new JButton("刷新");
        localRefreshButton.setFont(new Font(null, Font.PLAIN, 11));
        localRefreshButton.addActionListener(e -> onRefresh(tabIndex));

        searchPanel.add(localSearchField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(localSearchButton);
        buttonPanel.add(localRefreshButton);
        searchPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(searchPanel, BorderLayout.NORTH);

        // 表格
        String[] columnNames = getColumnNames(tabIndex);
        Vector<String> columnNameVector = new Vector<>(Arrays.asList(columnNames));
        Vector<Vector<Object>> dataVector = new Vector<>();

        DefaultTableModel localTableModel = new DefaultTableModel(dataVector, columnNameVector) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabTableModels.put(tabIndex, localTableModel);

        JTable localDataTable = new JBTable(localTableModel);
        localDataTable.setFont(new Font(null, Font.PLAIN, 11));
        localDataTable.setRowHeight(20);
        localDataTable.getTableHeader().setFont(new Font(null, Font.BOLD, 11));
        localDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 关闭自动调整模式，手动控制列宽
        localDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        final int finalTabIndex = tabIndex;
        localDataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = localDataTable.getSelectedRow();
                    if (row >= 0) {
                        onDoubleClick(finalTabIndex, row);
                    }
                }
            }
        });

        // 监听面板大小变化，动态调整列宽
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustColumnWidths(finalTabIndex);
            }
        });

        tabTables.put(tabIndex, localDataTable);

        JBScrollPane scrollPane = new JBScrollPane(localDataTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    protected void performSearch(int tabIndex) {
        JTextField localSearchField = tabSearchFields.get(tabIndex);
        DefaultTableModel localTableModel = tabTableModels.get(tabIndex);
        if (localSearchField == null || localTableModel == null) {
            return;
        }
        String searchText = localSearchField.getText().trim();
        Vector<Vector<Object>> rowData = getRowData(tabIndex, searchText);
        localTableModel.setDataVector(rowData, new Vector<>(Arrays.asList(getColumnNames(tabIndex))));
        adjustColumnWidths(tabIndex);
    }

    protected void adjustColumnWidths(int tabIndex) {
        JTable localDataTable = tabTables.get(tabIndex);
        if (localDataTable == null || localDataTable.getParent() == null) {
            return;
        }

        int columnCount = getColumnCount(tabIndex);
        if (columnCount == 0) {
            return;
        }

        // 获取表格可见宽度
        int tableWidth = localDataTable.getParent().getParent().getWidth();
        if (tableWidth <= 0) {
            tableWidth = 500; // 默认宽度
        }

        TableColumnModel columnModel = localDataTable.getColumnModel();

        // 获取列宽偏好设置
        int[] preferredWidths = getColumnPreferredWidths(tabIndex);
        int totalPreferredWidth = 0;

        for (int i = 0; i < columnCount && i < preferredWidths.length; i++) {
            totalPreferredWidth += preferredWidths[i];
        }

        // 如果总偏好宽度小于表格宽度，最后一列自动扩展
        if (totalPreferredWidth < tableWidth && columnCount > 0) {
            int extraWidth = tableWidth - totalPreferredWidth;
            int lastColumnIndex = columnCount - 1;
            int lastColumnPreferred = preferredWidths.length > lastColumnIndex ? preferredWidths[lastColumnIndex] : 100;
            columnModel.getColumn(lastColumnIndex).setPreferredWidth(lastColumnPreferred + extraWidth);

            // 设置其他列
            for (int i = 0; i < columnCount - 1 && i < preferredWidths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(preferredWidths[i]);
            }
        } else {
            // 使用预设宽度
            for (int i = 0; i < columnCount && i < preferredWidths.length; i++) {
                columnModel.getColumn(i).setPreferredWidth(preferredWidths[i]);
            }
            // 超出部分使用默认宽度
            for (int i = preferredWidths.length; i < columnCount; i++) {
                columnModel.getColumn(i).setPreferredWidth(100);
            }
        }
    }

    protected void onTabChanged(int tabIndex) {
        JTextField localSearchField = tabSearchFields.get(tabIndex);
        if (localSearchField != null) {
            localSearchField.setText("");
        }
        performSearch(tabIndex);
    }

    protected int[] getColumnPreferredWidths(int tabIndex) {
        int columnCount = getColumnCount(tabIndex);
        int[] widths = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
            widths[i] = 100; // 默认宽度
        }
        return widths;
    }

    protected abstract String[] getTabNames();

    protected abstract int getColumnCount(int tabIndex);

    protected abstract String[] getColumnNames(int tabIndex);

    protected abstract Vector<Vector<Object>> getRowData(int tabIndex, String searchText);

    protected abstract void onRefresh(int tabIndex);

    protected abstract void onDoubleClick(int tabIndex, int row);
}
