package com.jiuxiao.assistant.panel;

import com.intellij.openapi.project.Project;
import com.jiuxiao.assistant.enums.HandlerEnum;
import com.jiuxiao.assistant.enums.PanelEnum;
import com.jiuxiao.assistant.handler.StrDecodeHandler;
import com.jiuxiao.assistant.handler.StrEncodeHandler;
import com.jiuxiao.assistant.panel.base.BaseMultiFunctionPanel;
import com.jiuxiao.assistant.util.SystemHandleUtil;

import javax.swing.*;
import java.util.Objects;

public class SecurityCryptoPanel extends BaseMultiFunctionPanel {

    private final Project project;
    private final StrEncodeHandler strEncodeHandler;
    private final StrDecodeHandler strDecodeHandler;
    private JPanel currentFunctionPanel;

    public SecurityCryptoPanel(Project project) {
        this.project = project;
        this.panelTitle = PanelEnum.SECURITY_CRYPTO.getPanel();

        strEncodeHandler = new StrEncodeHandler();
        strDecodeHandler = new StrDecodeHandler();

        onFunctionChanged();
    }

    @Override
    protected void populateFunctionCombo(JComboBox<String> combo) {
        combo.addItem(HandlerEnum.STR_ENCODE.getFunction());
        combo.addItem(HandlerEnum.STR_DECODE.getFunction());
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
            case STR_ENCODE:
                currentFunctionPanel = strEncodeHandler.createPanel();
                break;
            case STR_DECODE:
                currentFunctionPanel = strDecodeHandler.createPanel();
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
                case STR_ENCODE:
                    result = strEncodeHandler.execute(input);
                    break;
                case STR_DECODE:
                    result = strDecodeHandler.execute(input);
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
            case STR_ENCODE:
                example = strEncodeHandler.getExample();
                break;
            case STR_DECODE:
                example = strDecodeHandler.getExample();
                break;
            default:
                return;
        }

        if (example != null) {
            inputArea.setText(example);
        }
    }
}
