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
 * Controller快速生成Mock数据动作类
 */
public class ControlMockAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiMethod method = CommonUtil.getValidMethodFromEvent(e);
        e.getPresentation().setVisible(PsiMethodUtil.isRequestControlMethod(method));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiMethod psiMethod = CommonUtil.getValidMethodFromEvent(e);
        if (Objects.isNull(psiMethod)) {
            return;
        }

        boolean controlMethod = PsiMethodUtil.isRequestControlMethod(psiMethod);
        if (BooleanUtils.isFalse(controlMethod)) {
            return;
        }

        RequestInfo requestInfo = PsiMethodUtil.getMethodRequestInfo(psiMethod);

        JSONObject jsonObject = new JSONObject(){{
            put("method", requestInfo.getRequestMethod());
            put("url", requestInfo.getUrl());
            put("body", requestInfo.getRequestBody());
        }};

        SystemHandleUtil.copyToClipboard(jsonObject.toJSONString());
        SystemHandleUtil.showStatusBarTip(
                e.getProject(), "Mock数据已复制到剪切板"
        );
    }
}
