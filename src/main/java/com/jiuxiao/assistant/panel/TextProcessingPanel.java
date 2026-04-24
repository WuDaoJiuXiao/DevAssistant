package com.jiuxiao.assistant.panel;

import com.intellij.openapi.project.Project;
import com.jiuxiao.assistant.enums.HandlerEnum;
import com.jiuxiao.assistant.enums.PanelEnum;
import com.jiuxiao.assistant.handler.NamingHandler;
import com.jiuxiao.assistant.handler.TrimSpaceHandler;
import com.jiuxiao.assistant.util.SystemHandleUtil;

import javax.swing.*;
import java.util.Objects;

public class TextProcessingPanel extends BaseMultiFunctionPanel {

    private final Project project;
    private final NamingHandler namingHandler;
    private final TrimSpaceHandler trimSpaceHandler;
    private JPanel currentFunctionPanel;

    public TextProcessingPanel(Project project) {
        this.project = project;
        this.panelTitle = PanelEnum.TEXT_PROCESSING.getPanel();

        namingHandler = new NamingHandler();
        trimSpaceHandler = new TrimSpaceHandler();

        onFunctionChanged();
    }

    @Override
    protected void populateFunctionCombo(JComboBox<String> combo) {
        combo.addItem(HandlerEnum.NAMING_SYNC.getFunction());
        combo.addItem(HandlerEnum.TRIM_SPACE.getFunction());
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
            case NAMING_SYNC:
                currentFunctionPanel = namingHandler.createPanel();
                break;
            case TRIM_SPACE:
                currentFunctionPanel = trimSpaceHandler.createPanel();
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
                case NAMING_SYNC:
                    result = namingHandler.execute(input);
                    break;
                case TRIM_SPACE:
                    result = trimSpaceHandler.execute(input);
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
            case NAMING_SYNC:
                example = namingHandler.getExample();
                break;
            case TRIM_SPACE:
                example = trimSpaceHandler.getExample();
                break;
            default:
                return;
        }

        if (example != null) {
            inputArea.setText(example);
        }
    }
}
