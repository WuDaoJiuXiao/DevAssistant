package com.jiuxiao.assistant.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 通用工具类
 */
public class CommonUtil {

    private static final String POUND_STRING = "#";

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

    /**
     * 构建方法在缓存中的唯一标识Key
     *
     * @param containingClass Mapper接口的PSI类对象
     * @param methodName      方法名称
     * @return 方法唯一标识Key，格式为: 全限定类名#方法名
     */
    public static String buildMethodKey(PsiClass containingClass, String methodName) {
        return containingClass.getQualifiedName() + POUND_STRING + methodName;
    }

    /**
     * 构建XML中SQL标签在缓存中的唯一标识Key
     *
     * @param namespace XML文件的namespace属性值，通常为Mapper接口的全限定名
     * @param sqlId     SQL标签的id属性值，对应Mapper接口中的方法名
     * @return XML标签唯一标识Key，格式为: namespace#sqlId
     */
    public static String buildXmlTagKey(String namespace, String sqlId) {
        return namespace + POUND_STRING + sqlId;
    }
}
