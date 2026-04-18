package com.jiuxiao.assistant.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiMethod;
import com.jiuxiao.assistant.dto.extract.MethodInfo;
import com.jiuxiao.assistant.generator.JsonGenerator;
import com.jiuxiao.assistant.resolver.MethodInfoResolver;
import com.jiuxiao.assistant.util.CommonUtil;
import com.jiuxiao.assistant.util.SystemHandleUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 提取方法全量信息的点击动作类
 * 继承自AnAction，用于处理IntelliJ IDEA插件中的用户动作
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public class ExtractMethodInfoAction extends AnAction {

    /**
     * 获取动作的更新线程
     *
     * @return 返回后台线程(BGT)，表示此动作的更新应该在后台线程执行
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * 更新动作的可见性和启用状态
     * 根据当前选中的元素是否为有效方法来决定动作是否可见
     *
     * @param e 动作事件对象，包含当前上下文信息
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiMethod method = CommonUtil.getValidMethodFromEvent(e);
        e.getPresentation().setVisible(Objects.nonNull(method));
    }

    /**
     * 动作执行时的处理逻辑
     * 当用户触发此动作时，提取方法信息并转换为JSON格式复制到剪贴板
     *
     * @param e 动作事件对象，包含当前上下文信息
     */
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

        String requestBody = JsonGenerator.generateRequestBody(methodInfo, true);
        SystemHandleUtil.copyToClipboard(requestBody);
        SystemHandleUtil.showStatusBarTip(
                e.getProject(), "JSON对象已复制到剪切板"
        );
    }
}
