package com.jiuxiao.assistant.action;

import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiMethod;
import com.jiuxiao.assistant.dto.mock.RequestInfo;
import com.jiuxiao.assistant.util.CommonUtil;
import com.jiuxiao.assistant.util.PsiMethodUtil;
import com.jiuxiao.assistant.util.SystemHandleUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 控制模拟请求的Action类
 * 用于处理IDEA中的模拟请求相关操作
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public class ControlMockAction extends AnAction {

    /**
     * 获取Action的更新线程
     *
     * @return 返回后台线程(BGT)
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * 更新Action的状态和可见性
     *
     * @param e AnActionEvent事件对象，包含当前上下文信息
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 从事件中获取有效的方法
        PsiMethod method = CommonUtil.getValidMethodFromEvent(e);
        // 根据方法是否为请求控制方法来设置Action的可见性
        e.getPresentation().setVisible(PsiMethodUtil.isRequestControlMethod(method));
    }

    /**
     * 处理Action的执行逻辑
     *
     * @param e AnActionEvent事件对象，包含当前上下文信息
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 从事件中获取有效的方法
        PsiMethod psiMethod = CommonUtil.getValidMethodFromEvent(e);
        // 如果方法为空，直接返回
        if (Objects.isNull(psiMethod)) {
            return;
        }

        // 检查是否为请求控制方法
        boolean controlMethod = PsiMethodUtil.isRequestControlMethod(psiMethod);
        if (BooleanUtils.isFalse(controlMethod)) {
            return;
        }

        // 获取方法的请求信息
        RequestInfo requestInfo = PsiMethodUtil.getMethodRequestInfo(psiMethod);

        // 构建包含请求信息的JSON对象
        JSONObject jsonObject = new JSONObject() {{
            put("method", requestInfo.getRequestMethod());
            put("url", requestInfo.getUrl());
            put("body", requestInfo.getRequestBody());
        }};
        // 将JSON数据复制到剪贴板

        // 显示状态栏提示信息
        SystemHandleUtil.copyToClipboard(jsonObject.toJSONString());
        SystemHandleUtil.showStatusBarTip(
                e.getProject(), "Mock数据已复制到剪切板"
        );
    }
}
