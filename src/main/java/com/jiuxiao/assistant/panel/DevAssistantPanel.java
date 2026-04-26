package com.jiuxiao.assistant.panel;

import com.intellij.openapi.project.Project;
import com.jiuxiao.assistant.enums.HandlerEnum;
import com.jiuxiao.assistant.enums.PanelEnum;
import com.jiuxiao.assistant.handler.DataGenerationHandler;
import com.jiuxiao.assistant.handler.MachineInfoHandler;
import com.jiuxiao.assistant.handler.MybatisSqlHandler;
import com.jiuxiao.assistant.handler.PatternHandler;
import com.jiuxiao.assistant.panel.base.BaseMultiFunctionPanel;
import com.jiuxiao.assistant.util.SystemHandleUtil;

import javax.swing.*;
import java.util.Objects;

public class DevAssistantPanel extends BaseMultiFunctionPanel {

    private final Project project;
    private final PatternHandler patternHandler;
    private final DataGenerationHandler dataGenerationHandler;
    private final MachineInfoHandler machineInfoHandler;
    private final MybatisSqlHandler mybatisSqlHandler;
    private JPanel currentFunctionPanel;

    public DevAssistantPanel(Project project) {
        this.project = project;
        this.panelTitle = PanelEnum.DEV_ASSISTANT.getPanel();

        patternHandler = new PatternHandler();
        dataGenerationHandler = new DataGenerationHandler();
        machineInfoHandler = new MachineInfoHandler();
        mybatisSqlHandler = new MybatisSqlHandler();

        onFunctionChanged();
    }

    @Override
    protected void populateFunctionCombo(JComboBox<String> combo) {
        combo.addItem(HandlerEnum.PATTERN.getFunction());
        combo.addItem(HandlerEnum.DATA_GENERATION.getFunction());
        combo.addItem(HandlerEnum.MACHINE_INFORMATION.getFunction());
        combo.addItem(HandlerEnum.MYBATIS_SQL.getFunction());
    }

    @Override
    protected void onFunctionChanged() {
        String selected = getCurrentFunction();

        if (currentFunctionPanel != null) {
            dynamicButtonPanel.remove(currentFunctionPanel);
        }

        HandlerEnum handlerEnum = HandlerEnum.findByFunction(selected);
        if (Objects.isNull(handlerEnum)) {
            return;
        }

        switch (handlerEnum) {
            case PATTERN:
                currentFunctionPanel = patternHandler.createPanel();
                break;
            case DATA_GENERATION:
                currentFunctionPanel = dataGenerationHandler.createPanel();
                break;
            case MACHINE_INFORMATION:
                currentFunctionPanel = machineInfoHandler.createPanel();
                break;
            case MYBATIS_SQL:
                currentFunctionPanel = mybatisSqlHandler.createPanel();
                break;
            default:
                break;
        }

        if (currentFunctionPanel != null) {
            dynamicButtonPanel.add(currentFunctionPanel);
        }

        dynamicButtonPanel.revalidate();
        dynamicButtonPanel.repaint();

        loadExampleForCurrentFunction();
    }

    @Override
    protected void execute() {
        String selected = getCurrentFunction();
        HandlerEnum handlerEnum = HandlerEnum.findByFunction(selected);
        if (Objects.isNull(handlerEnum)) {
            return;
        }

        String result;
        String input = getInput();

        try {
            switch (handlerEnum) {
                case PATTERN:
                    result = patternHandler.execute(input);
                    break;
                case DATA_GENERATION:
                    result = dataGenerationHandler.execute(input);
                    break;
                case MACHINE_INFORMATION:
                    result = machineInfoHandler.execute(input);
                    break;
                case MYBATIS_SQL:
                    result = mybatisSqlHandler.execute(input);
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

    private void loadExampleForCurrentFunction() {
        String selected = getCurrentFunction();
        HandlerEnum handlerEnum = HandlerEnum.findByFunction(selected);
        if (Objects.isNull(handlerEnum)) {
            return;
        }

        String example;
        switch (handlerEnum) {
            case PATTERN:
                example = patternHandler.getExample();
                break;
            case DATA_GENERATION:
                example = dataGenerationHandler.getExample();
                break;
            case MACHINE_INFORMATION:
                example = machineInfoHandler.getExample();
                break;
            case MYBATIS_SQL:
                example = mybatisSqlHandler.getExample();
                break;
            default:
                return;
        }

        if (example != null) {
            inputArea.setText(example);
        }
    }
}
