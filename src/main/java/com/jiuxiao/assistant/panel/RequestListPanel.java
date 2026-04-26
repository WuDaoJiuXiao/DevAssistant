package com.jiuxiao.assistant.panel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.handler.RequestListHandler;
import com.jiuxiao.assistant.service.ControllerRequestCacheService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

/**
 * 请求列表面板类，用于显示和管理控制器请求列表
 * 该类继承自JPanel，提供了一个可搜索的请求列表界面，支持双击跳转到对应控制器方法
 *
 * @author 悟道九霄
 * @date 2026/4/26
 */
public class RequestListPanel extends JPanel {

    private final Project project;  // 当前项目实例
    private boolean initialized = false;  // 初始化标志
    private JTable dataTable;  // 数据表格组件
    private DefaultTableModel tableModel;  // 表格数据模型
    private JTextField searchField;  // 搜索输入框
    private final RequestListHandler requestListHandler = new RequestListHandler();  // 请求列表处理器

    /**
     * 构造函数
     * 初始化请求列表面板，设置项目实例并初始化UI和处理器
     *
     * @param project 当前项目实例
     */
    public RequestListPanel(Project project) {
        this.project = project;
        initUI();  // 初始化用户界面
        initHandler();  // 初始化处理器
    }

    /**
     * 初始化用户界面
     * 设置面板布局，创建搜索面板和数据表格
     */
    private void initUI() {
        setLayout(new BorderLayout(5, 5));  // 设置边界布局，组件间距为5像素
        JPanel searchPanel = createSearchPanel();  // 创建搜索面板
        add(searchPanel, BorderLayout.NORTH);  // 将搜索面板添加到北部区域
        createTable();  // 创建数据表格
        performSearch();  // 执行初始搜索
    }

    /**
     * 创建搜索面板
     * 包含搜索输入框和重置按钮，支持实时搜索功能
     *
     * @return 返回配置好的搜索面板
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));  // 创建边界布局的搜索面板，水平间距5像素
        searchPanel.setBorder(JBUI.Borders.empty(5));  // 设置5像素的边距

        searchField = new JTextField();  // 创建搜索输入框
        searchField.setFont(new Font(null, Font.PLAIN, 11));  // 设置字体
        // 添加文档监听器，实现实时搜索功能
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();  // 插入文本时执行搜索
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();  // 删除文本时执行搜索
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch();  // 文档改变时执行搜索
            }
        });

        JButton searchButton = new JButton("重置搜索");  // 创建重置按钮
        searchButton.setFont(new Font(null, Font.PLAIN, 11));  // 设置字体
        searchButton.addActionListener(e -> performReset());  // 添加点击事件监听器

        searchPanel.add(searchField, BorderLayout.CENTER);  // 将搜索框添加到中间区域

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));  // 创建按钮面板
        buttonPanel.add(searchButton);  // 添加重置按钮
        searchPanel.add(buttonPanel, BorderLayout.EAST);  // 将按钮面板添加到东部区域

        return searchPanel;  // 返回配置好的搜索面板
    }

    /**
     * 创建数据表格
     * 设置表格的列名、数据模型、样式和事件监听器
     */
    private void createTable() {
        // 定义表格列名
        String[] columnNames = {"请求方式", "请求路径（双击可快速跳转）"};
        Vector<String> columnNameVector = new Vector<>(Arrays.asList(columnNames));

        // 创建表格数据模型，设置单元格不可编辑
        tableModel = new DefaultTableModel(new Vector<>(), columnNameVector) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // 所有单元格都不可编辑
            }
        };

        // 创建JBTable实例
        dataTable = new JBTable(tableModel);
        dataTable.setFont(new Font(null, Font.PLAIN, 11));  // 设置字体
        dataTable.setRowHeight(20);  // 设置行高
        dataTable.getTableHeader().setFont(new Font(null, Font.BOLD, 11));  // 设置表头字体
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // 设置单选模式
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);  // 关闭自动调整大小

        // 添加鼠标双击事件监听器
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // 检测双击事件
                    int row = dataTable.getSelectedRow();  // 获取选中的行
                    if (row >= 0) {  // 确保行号有效
                        onDoubleClick(row);  // 处理双击事件
                    }
                }
            }
        });

        // 监听面板大小变化，动态调整列宽
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustColumnWidths();  // 调整列宽
            }
        });

        // 创建滚动面板并添加表格
        JBScrollPane scrollPane = new JBScrollPane(dataTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);  // 垂直滚动条根据需要显示
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);  // 水平滚动条不显示
        add(scrollPane, BorderLayout.CENTER);  // 将滚动面板添加到中间区域
    }

    /**
     * 调整表格列宽
     * 根据面板大小动态调整请求方式列和请求路径列的宽度
     */
    private void adjustColumnWidths() {
        if (dataTable == null || dataTable.getParent() == null) return;

        // 获取表格父容器的宽度
        int tableWidth = dataTable.getParent().getParent().getWidth();
        if (tableWidth <= 0) tableWidth = 500;  // 默认宽度



        // 计算各列宽度
        int methodColumnWidth = 80;  // 请求方式列固定宽度
        int pathColumnWidth = tableWidth - methodColumnWidth - 20;  // 请求路径列剩余宽度
        if (pathColumnWidth < 100) pathColumnWidth = 100;  // 最小宽度限制

        // 设置列宽
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(methodColumnWidth);
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(pathColumnWidth);
    }

    /**
     * 初始化处理器
     * 设置缓存服务并添加初始化回调
     */
    private void initHandler() {
        ControllerRequestCacheService cacheService = project.getService(ControllerRequestCacheService.class);
        if (cacheService != null) {
            requestListHandler.setCacheService(cacheService);  // 设置缓存服务
            initialized = true;  // 标记已初始化
            // 添加初始化回调，在缓存初始化完成后执行搜索
            cacheService.addInitCallback(() -> SwingUtilities.invokeLater(this::performSearch));
            // 如果缓存不为空，立即执行搜索
            if (!cacheService.getRequestMappingInfoList().isEmpty()) {
                SwingUtilities.invokeLater(this::performSearch);
            }
        }
    }

    /**
     * 执行搜索
     * 根据搜索框中的文本过滤并更新表格数据
     */
    private void performSearch() {
        if (!initialized) return;  // 如果未初始化则不执行
        String searchText = searchField.getText().trim();  // 获取搜索文本
        Vector<Vector<Object>> rowData = requestListHandler.getRowData(0, searchText);  // 获取匹配的行数据
        tableModel.setRowCount(0);  // 清空表格
        // 添加新数据到表格
        for (Vector<Object> row : rowData) {
            tableModel.addRow(row);
        }
        adjustColumnWidths();  // 调整列宽
    }

    /**
     * 重置搜索
     * 清空搜索框并重新加载数据
     */
    private void performReset() {
        searchField.setText(null);  // 清空搜索框
    }

    /**
     * 处理表格双击事件
     * 根据双击的行号跳转到对应的URL
     *
     * @param row 双击的行号
     */
    private void onDoubleClick(int row) {
        if (!initialized) return;  // 如果未初始化则不执行
        String url = (String) tableModel.getValueAt(row, 1);  // 获取URL
        if (url != null && !url.isEmpty()) {  // 确保URL有效
            requestListHandler.navigateToUrl(url);  // 跳转到URL
        }
    }
}
