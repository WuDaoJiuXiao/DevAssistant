package com.jiuxiao.assistant.panel;

import com.intellij.openapi.project.Project;
import com.jiuxiao.assistant.enums.HandlerEnum;
import com.jiuxiao.assistant.enums.PanelEnum;
import com.jiuxiao.assistant.handler.*;
import com.jiuxiao.assistant.handler.JsonSyncXmlHandler;
import com.jiuxiao.assistant.util.SystemHandleUtil;

import javax.swing.*;
import java.util.Objects;

/**
 * 数据转换Tab面板
 * 该类继承自BaseMultiFunctionPanel，实现了数据转换相关的功能界面
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public class DataConversionPanel extends BaseMultiFunctionPanel {

    private final Project project;
    private final TimestampHandler timestampHandler;
    private final PayloadToJsonHandler payloadToJsonHandler;
    private final JsonSyncJavaHandler jsonSyncJavaHandler;
    private final JsonSyncXmlHandler jsonSyncXmlHandler;
    private final BinaryHandler binaryHandler;
    private JPanel currentFunctionPanel;

    /**
     * 构造函数
     *
     * @param project 当前项目对象
     */
    public DataConversionPanel(Project project) {
        this.project = project;
        this.panelTitle = PanelEnum.FORMAT_CONVERSION.getPanel();

        timestampHandler = new TimestampHandler();
        payloadToJsonHandler = new PayloadToJsonHandler();
        jsonSyncJavaHandler = new JsonSyncJavaHandler();
        jsonSyncXmlHandler = new JsonSyncXmlHandler();
        binaryHandler = new BinaryHandler();

        onFunctionChanged();
    }

    /**
     * 填充功能下拉框选项
     *
     * @param combo 功能选择下拉框
     */
    @Override
    protected void populateFunctionCombo(JComboBox<String> combo) {
        combo.addItem(HandlerEnum.TIMESTAMP_SYNC_DATE.getFunction());
        combo.addItem(HandlerEnum.PAYLOAD_TO_JSON.getFunction());
        combo.addItem(HandlerEnum.JSON_SYNC_JAVA.getFunction());
        combo.addItem(HandlerEnum.JSON_SYNC_XML.getFunction());
        combo.addItem(HandlerEnum.BINARY_SYNC.getFunction());
    }

    /**
     * 功能切换处理方法
     * 当用户切换功能时，会调用此方法来更新界面显示
     */
    @Override
    protected void onFunctionChanged() {
        String selected = getCurrentFunction();

        // 移除旧的功能面板
        if (currentFunctionPanel != null) {
            dynamicButtonPanel.remove(currentFunctionPanel);
        }

        HandlerEnum handlerEnum = HandlerEnum.findByFunction(selected);
        if (Objects.isNull(handlerEnum)) {
            return;
        }

        switch (handlerEnum) {
            case TIMESTAMP_SYNC_DATE:
                currentFunctionPanel = timestampHandler.createPanel();
                break;
            case PAYLOAD_TO_JSON:
                currentFunctionPanel = payloadToJsonHandler.createPanel();
                break;
            case JSON_SYNC_JAVA:
                currentFunctionPanel = jsonSyncJavaHandler.createPanel();
                break;
            case JSON_SYNC_XML:
                currentFunctionPanel = jsonSyncXmlHandler.createPanel();
                break;
            case BINARY_SYNC:
                currentFunctionPanel = binaryHandler.createPanel();
                break;
            default:
                break;
        }

        // 添加新的功能面板
        if (currentFunctionPanel != null) {
            dynamicButtonPanel.add(currentFunctionPanel);
        }

        // 刷新UI
        dynamicButtonPanel.revalidate();
        dynamicButtonPanel.repaint();

        // 加载示例数据
        loadExampleForCurrentFunction();
    }

    /**
     * 执行当前选中的功能
     * 验证输入并调用相应的处理器执行转换
     */
    @Override
    protected void execute() {
        if (validateInput()) {
            return;
        }

        String selected = getCurrentFunction();
        HandlerEnum handlerEnum = HandlerEnum.findByFunction(selected);
        if (Objects.isNull(handlerEnum)) {
            return;
        }

        String result;
        String input = getInput();

        try {
            switch (handlerEnum) {
                case TIMESTAMP_SYNC_DATE:
                    result = timestampHandler.execute(input);
                    break;
                case PAYLOAD_TO_JSON:
                    result = payloadToJsonHandler.execute(input);
                    break;
                case JSON_SYNC_JAVA:
                    result = jsonSyncJavaHandler.execute(input);
                    break;
                case JSON_SYNC_XML:
                    result = jsonSyncXmlHandler.execute(input);
                    break;
                case BINARY_SYNC:
                    result = binaryHandler.execute(input);
                    break;
                default:
                    result = "未选择功能";
            }
            setOutput(result);
        } catch (Exception e) {
            SystemHandleUtil.showError(project, "执行失败: " + e.getMessage());
            setOutput("错误: " + e.getMessage());
        }
    }

    /**
     * 加载当前功能的示例数据
     * 根据当前选中的功能，加载对应的示例数据到输入框
     */
    private void loadExampleForCurrentFunction() {
        String selected = getCurrentFunction();
        HandlerEnum handlerEnum = HandlerEnum.findByFunction(selected);
        if (Objects.isNull(handlerEnum)) {
            return;
        }

        String example;
        switch (handlerEnum) {
            case TIMESTAMP_SYNC_DATE:
                example = timestampHandler.getExample();
                break;
            case PAYLOAD_TO_JSON:
                example = payloadToJsonHandler.getExample();
                break;
            case JSON_SYNC_JAVA:
                example = jsonSyncJavaHandler.getExample();
                break;
            case JSON_SYNC_XML:
                example = jsonSyncXmlHandler.getExample();
                break;
            case BINARY_SYNC:
                example = binaryHandler.getExample();
                break;
            default:
                return;
        }

        if (example != null) {
            inputArea.setText(example);
        }
    }
}