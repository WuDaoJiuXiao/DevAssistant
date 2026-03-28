package com.jiuxiao.assistant.util;

import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Psi方法通用工具类
 */
public class PsiMethodUtil {

    private static final String COLLECTION_STRING = "java.util.Collection";

    private static final String MAP_STRING = "java.util.Map";

    private static final String LANG_STRING = "java.lang.String";

    /**
     * 从偏移量定位当前的 PsiMethod
     *
     * @param psiFile 当前的文档对象
     * @param offset  光标偏移量
     * @return 当前光标定位的方法对象
     */
    @Nullable
    public static PsiMethod findMethodAtOffset(@NotNull PsiFile psiFile, int offset) {
        PsiElement elementAt = psiFile.findElementAt(offset);
        if (Objects.isNull(elementAt)) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class, false);
    }

    /**
     * 校验方法是否为有效方法: 类/接口中的有效方法
     *
     * @param psiMethod 方法对象
     * @return 是否有效
     */
    public static boolean checkIsValidMethod(@NotNull PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (Objects.isNull(containingClass)) {
            return false;
        }

        boolean anEnum = containingClass.isEnum();
        boolean annotationType = containingClass.isAnnotationType();
        return !(anEnum || annotationType);
    }

    /**
     * 校验是否为 String 类型
     *
     * @param psiClass 被校验对象
     * @return 是否为 String 类型
     */
    public static boolean isJavaLangString(PsiClass psiClass) {
        boolean nonNull = Objects.nonNull(psiClass);
        boolean equals = StringUtils.equals(LANG_STRING, psiClass.getQualifiedName());
        return nonNull && equals;
    }

    /**
     * 校验是否为普通接口
     *
     * @param psiClass 被校验对象
     * @return 是否为函数式接口
     */
    public static boolean isNormalInterface(PsiClass psiClass) {
        return psiClass.isInterface();
    }

    /**
     * 校验是否为函数式接口
     *
     * @param psiClass 被校验对象
     * @return 是否为函数式接口
     */
    public static boolean isFunctionalInterface(PsiClass psiClass) {
        if (Objects.isNull(psiClass) || !psiClass.isInterface()) {
            return false;
        }
        PsiMethod[] methods = psiClass.getMethods();
        int abstractCount = 0;
        for (PsiMethod method : methods) {
            boolean isAbstract = method.hasModifierProperty(PsiModifier.ABSTRACT);
            boolean isDefault = method.hasModifierProperty(PsiModifier.DEFAULT);
            boolean isStatic = method.hasModifierProperty(PsiModifier.STATIC);
            if (isAbstract && !isDefault && !isStatic) {
                abstractCount++;
            }
        }
        return abstractCount == 1;
    }

    /**
     * 校验是否为枚举类型
     *
     * @param psiClass 被校验对象
     * @return 是否为枚举类型
     */
    public static boolean isEnum(PsiClass psiClass) {
        return psiClass.isEnum();
    }

    /**
     * 校验是否为集合类型
     *
     * @param psiClass 被校验对象
     * @return 是否为集合类型
     */
    public static boolean isCollection(PsiClass psiClass) {
        return isSubClass(psiClass, COLLECTION_STRING);
    }

    /**
     * 校验是否为哈希类型
     *
     * @param psiClass 被校验对象
     * @return 是否为哈希类型
     */
    public static boolean isMap(PsiClass psiClass) {
        return isSubClass(psiClass, MAP_STRING);
    }

    /**
     * 判断是否为实现类/子类关系
     *
     * @param psiClass      被校验对象
     * @param qualifiedName 目标对象全限定名
     * @return 是否为实现类/子类关系
     */
    private static boolean isSubClass(PsiClass psiClass, String qualifiedName) {
        if (Objects.isNull(psiClass) || StringUtils.isEmpty(qualifiedName)) {
            return false;
        }
        return InheritanceUtil.isInheritor(psiClass, qualifiedName);
    }
}
