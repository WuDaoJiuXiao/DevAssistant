package com.jiuxiao.assistant.util;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解工具类
 */
public class AnnoUtil {

    /**
     * 获取注解指定属性的值
     *
     * @param annotation    注解对象
     * @param attributeName 属性名
     * @return 属性值数组
     */
    public static String[] getAttributeValues(PsiAnnotation annotation, String attributeName) {
        if (annotation == null) return new String[0];

        PsiAnnotationMemberValue value = annotation.findAttributeValue(attributeName);
        if (value == null) return new String[0];

        List<String> result = new ArrayList<>();

        if (value instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue arrayValue = (PsiArrayInitializerMemberValue) value;
            for (PsiAnnotationMemberValue initializer : arrayValue.getInitializers()) {
                addSingleValue(initializer, result);
            }
        } else {
            addSingleValue(value, result);
        }

        return result.toArray(new String[0]);
    }


    /**
     * 解析单个值
     *
     * @param value  值对象
     * @param result 解析结果
     */
    private static void addSingleValue(PsiAnnotationMemberValue value, List<String> result) {
        if (value == null) return;

        if (value instanceof PsiLiteralExpression) {
            PsiLiteralExpression literal = (PsiLiteralExpression) value;
            Object val = literal.getValue();
            if (val instanceof String) {
                result.add((String) val);
            }
        } else if (value instanceof PsiReferenceExpression) {
            PsiReferenceExpression ref = (PsiReferenceExpression) value;
            PsiElement resolved = ref.resolve();
            if (resolved instanceof PsiEnumConstant) {
                PsiEnumConstant enumConstant = (PsiEnumConstant) resolved;
                result.add(enumConstant.getName());
            }
        }
    }
}
