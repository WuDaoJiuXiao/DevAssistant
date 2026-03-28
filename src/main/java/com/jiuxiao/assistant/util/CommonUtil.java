package com.jiuxiao.assistant.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 通用工具类
 */
public class CommonUtil {

    /**
     * 获取当前光标所在的方法对象
     *
     * @param event 动作事件
     * @return 当前光标所在的方法对象
     */
    public static PsiMethod getValidMethodFromEvent(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

        if (editor == null || !(psiFile instanceof PsiJavaFile)) {
            return null;
        }

        // 根据偏移量定位当前方法
        int offset = editor.getCaretModel().getOffset();
        PsiMethod psiMethod = PsiMethodUtil.findMethodAtOffset(psiFile, offset);
        if (Objects.isNull(psiMethod)) {
            return null;
        }

        // 必须是有效方法
        if (!PsiMethodUtil.checkIsValidMethod(psiMethod)) {
            return null;
        }

        return psiMethod;
    }
}
