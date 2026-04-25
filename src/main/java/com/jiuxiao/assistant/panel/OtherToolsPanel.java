package com.jiuxiao.assistant.panel;

import com.intellij.openapi.project.Project;
import com.intellij.ui.Gray;
import com.intellij.util.ui.JBUI;
import com.jiuxiao.assistant.enums.HandlerEnum;
import com.jiuxiao.assistant.handler.RequestListHandler;
import com.jiuxiao.assistant.service.ControllerRequestCacheService;
import com.jiuxiao.assistant.util.SystemHandleUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.Vector;

/**
 * OtherToolsPanel类是一个继承自BaseListFunctionPanel的面板类，用于展示项目中的其他工具面板
 *
 * @author 悟道九霄
 * @date 2026/4/18
 */
public class OtherToolsPanel extends BaseListFunctionPanel {

    /**
     * 当前项目实例
     */
    private final Project project;
    /**
     * 请求列表处理器，用于处理请求相关的操作
     */
    private final RequestListHandler requestListHandler = new RequestListHandler();
    /**
     * 标记是否已初始化
     */
    private boolean initialized = false;

    /**
     * 构造函数，初始化OtherToolsPanel
     *
     * @param project 当前项目实例
     */
    public OtherToolsPanel(Project project) {
        this.project = project;
        initHandler();
        addTipLabel();
    }

    /**
     * 添加提示标签到搜索面板
     * 在按钮后面添加"新增请求后点刷新"的提示信息
     */
    private void addTipLabel() {
        SwingUtilities.invokeLater(() -> {
            JPanel firstTabPanel = tabPanels.get(0);
            if (firstTabPanel != null) {
                // 找到搜索面板中的按钮面板
                Component[] components = firstTabPanel.getComponents();
                if (components.length > 0 && components[0] instanceof JPanel) {
                    JPanel searchPanel = (JPanel) components[0];
                    // 获取按钮面板（BorderLayout.EAST 位置的组件）
                    Component eastComp = ((BorderLayout) searchPanel.getLayout()).getLayoutComponent(BorderLayout.EAST);
                    if (eastComp instanceof JPanel) {
                        JPanel buttonPanel = (JPanel) eastComp;
                        // 在按钮后面添加提示标签
                        JLabel tipLabel = new JLabel("💡 新增请求后点刷新");
                        tipLabel.setFont(new Font(null, Font.PLAIN, 10));
                        tipLabel.setForeground(Gray._100);
                        tipLabel.setBorder(JBUI.Borders.emptyLeft(10));
                        buttonPanel.add(tipLabel);
                        buttonPanel.revalidate();
                        buttonPanel.repaint();
                    }
                }
            }
        });
    }

    /**
     * 初始化处理器
     * 设置缓存服务并添加初始化回调
     */
    private void initHandler() {
        ControllerRequestCacheService cacheService = project.getService(ControllerRequestCacheService.class);
        if (cacheService != null) {
            requestListHandler.setCacheService(cacheService);
            initialized = true;
            cacheService.addInitCallback(() -> SwingUtilities.invokeLater(() -> {
                if (tabbedPane != null && tabbedPane.getSelectedIndex() == 0) {
                    performSearch(0);
                }
            }));
            // 如果已经有数据，立即刷新
            if (!cacheService.getRequestMappingInfoList().isEmpty()) {
                SwingUtilities.invokeLater(() -> performSearch(0));
            }
        }
    }

    /**
     * 获取标签页名称
     *
     * @return 包含"项目请求列表"和"Maven依赖"的字符串数组
     */
    @Override
    protected String[] getTabNames() {
        return new String[]{
                HandlerEnum.REQUEST_LIST.getFunction(),
                HandlerEnum.MAVEN_DEPENDENCY.getFunction()
        };
    }

    /**
     * 获取指定标签页的列数
     *
     * @param tabIndex 标签页索引
     * @return 列数，第一个标签页返回2，其他返回0
     */
    @Override
    protected int getColumnCount(int tabIndex) {
        if (tabIndex == 0) {
            return 2;
        }
        return 0;
    }

    /**
     * 获取指定标签页的列名
     *
     * @param tabIndex 标签页索引
     * @return 列名数组，第一个标签页返回请求方式和请求路径，其他返回空数组
     */
    @Override
    protected String[] getColumnNames(int tabIndex) {
        if (tabIndex == 0) {
            return new String[]{"请求方式", "请求路径（双击可快速跳转）"};
        }
        return new String[]{};
    }

    /**
     * 获取列的推荐宽度
     *
     * @param tabIndex 标签页索引
     * @return 列宽度数组，第一个标签页返回[80, 200]，其他返回空数组
     */
    @Override
    protected int[] getColumnPreferredWidths(int tabIndex) {
        if (tabIndex == 0) {
            return new int[]{80, 200};
        }
        return new int[]{};
    }

    /**
     * 获取行数据
     *
     * @param tabIndex   标签页索引
     * @param searchText 搜索文本
     * @return 行数据向量，未初始化时返回空向量
     */
    @Override
    protected Vector<Vector<Object>> getRowData(int tabIndex, String searchText) {
        if (!initialized) {
            return new Vector<>();
        }
        if (tabIndex == 0) {
            return requestListHandler.getRowData(tabIndex, searchText);
        }
        return new Vector<>();
    }

    /**
     * 调整列宽
     *
     * @param tabIndex 标签页索引
     */
    @Override
    protected void adjustColumnWidths(int tabIndex) {
        if (tabIndex != 0 || !initialized) {
            return;
        }

        JTable localDataTable = tabTables.get(tabIndex);
        if (localDataTable == null || localDataTable.getParent() == null) {
            return;
        }

        int tableWidth = localDataTable.getParent().getParent().getWidth();
        if (tableWidth <= 0) {
            tableWidth = 500;
        }

        int methodColumnWidth = 80;
        int pathColumnWidth = tableWidth - methodColumnWidth - 20;

        if (pathColumnWidth < 100) {
            pathColumnWidth = 100;
        }

        localDataTable.getColumnModel().getColumn(0).setPreferredWidth(methodColumnWidth);
        localDataTable.getColumnModel().getColumn(1).setPreferredWidth(pathColumnWidth);
    }

    /**
     * 执行搜索
     *
     * @param tabIndex 标签页索引
     */
    @Override
    protected void performSearch(int tabIndex) {
        if (!initialized) {
            return;
        }
        if (tabIndex == 0) {
            JTextField localSearchField = tabSearchFields.get(tabIndex);
            DefaultTableModel localTableModel = tabTableModels.get(tabIndex);

            if (localSearchField == null || localTableModel == null) {
                return;
            }

            String searchText = localSearchField.getText().trim();
            Vector<Vector<Object>> rowData = requestListHandler.getRowData(tabIndex, searchText);
            localTableModel.setDataVector(rowData, new Vector<>(Arrays.asList(getColumnNames(tabIndex))));
            adjustColumnWidths(tabIndex);
        }
    }

    /**
     * 刷新操作
     *
     * @param tabIndex 标签页索引
     */
    @Override
    protected void onRefresh(int tabIndex) {
        if (!initialized) {
            return;
        }
        if (tabIndex == 0) {
            ControllerRequestCacheService cacheService = project.getService(ControllerRequestCacheService.class);
            if (cacheService != null) {
                cacheService.refreshControllerRequestMap();
                SwingUtilities.invokeLater(() -> adjustColumnWidths(tabIndex));
                SystemHandleUtil.showInformation(project, "索引已刷新");
            }
        }
    }

    /**
     * 处理双击事件
     *
     * @param tabIndex 标签页索引
     * @param row      点击的行索引
     */
    @Override
    protected void onDoubleClick(int tabIndex, int row) {
        if (!initialized) {
            return;
        }
        if (tabIndex == 0 && row >= 0) {
            DefaultTableModel localTableModel = tabTableModels.get(tabIndex);
            if (localTableModel == null) {
                return;
            }

            String url = (String) localTableModel.getValueAt(row, 1);
            if (url != null && !url.isEmpty()) {
                requestListHandler.navigateToUrl(url);
            }
        }
    }
}