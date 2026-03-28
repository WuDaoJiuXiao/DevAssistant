package com.jiuxiao.assistant.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiMethod;
import com.jiuxiao.assistant.dto.MethodInfo;
import com.jiuxiao.assistant.generator.JsonGenerator;
import com.jiuxiao.assistant.resolver.MethodInfoResolver;
import com.jiuxiao.assistant.util.CommonUtil;
import com.jiuxiao.assistant.util.SystemHandleUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 提取方法全量信息的点击动作类
 */
public class ExtractMethodInfoAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiMethod method = CommonUtil.getValidMethodFromEvent(e);
        e.getPresentation().setVisible(Objects.nonNull(method));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiMethod psiMethod = CommonUtil.getValidMethodFromEvent(e);
        if (Objects.isNull(psiMethod)) {
            return;
        }

        MethodInfo methodInfo = MethodInfoResolver.resolveMethodInfo(psiMethod);
        if (Objects.isNull(methodInfo)) {
            return;
        }

        // 将最终转换JSON字符串复制到剪切板
        String requestBody = JsonGenerator.generateRequestBody(methodInfo);
        SystemHandleUtil.copyToClipboard(requestBody);
        SystemHandleUtil.showMessage("JSON对象已复制到剪切板\nJSON string has been copied to the clipboard");
    }
}
