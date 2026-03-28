package com.jiuxiao.assistant.resolver;

import com.intellij.psi.*;
import com.jiuxiao.assistant.dto.FieldInfo;
import com.jiuxiao.assistant.dto.MethodInfo;
import com.jiuxiao.assistant.dto.MethodParamInfo;
import com.jiuxiao.assistant.dto.TypeInfo;
import com.jiuxiao.assistant.enums.FieldTypeEnum;
import com.jiuxiao.assistant.util.PsiMethodUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 方法信息解析器
 */
public class MethodInfoResolver {

    private static final String OBJECT_STRING = "java.lang.Object";

    /**
     * 解析方法为Info对象
     *
     * @param psiMethod 方法对象
     * @return 方法信息实体类
     */
    public static MethodInfo resolveMethodInfo(PsiMethod psiMethod) {
        if (Objects.isNull(psiMethod)) {
            return null;
        }

        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName(psiMethod.getName());
        methodInfo.setModifiers(resolveModifiers(psiMethod));
        methodInfo.setReType(resolveType(psiMethod.getReturnType()));
        methodInfo.setParameters(resolveMethodParams(psiMethod));
        return methodInfo;
    }

    /**
     * 解析方法的修饰符
     *
     * @param psiMethod 方法对象
     * @return 修饰符集合
     */
    private static Set<String> resolveModifiers(PsiMethod psiMethod) {
        Set<String> modifiers = new LinkedHashSet<>();
        for (String modifier : PsiModifier.MODIFIERS) {
            if (psiMethod.hasModifierProperty(modifier)) {
                modifiers.add(modifier);
            }
        }
        return modifiers;
    }

    /**
     * 解析方法的参数列表
     *
     * @param psiMethod 方法对象
     * @return 方法参数集合
     */
    private static List<MethodParamInfo> resolveMethodParams(PsiMethod psiMethod) {
        List<MethodParamInfo> paramInfoList = new ArrayList<>();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        for (PsiParameter parameter : parameters) {
            MethodParamInfo methodParamInfo = new MethodParamInfo();
            methodParamInfo.setParamName(parameter.getName());
            methodParamInfo.setParamType(resolveType(parameter.getType()));
            paramInfoList.add(methodParamInfo);
        }
        return paramInfoList;
    }

    /**
     * 解析参数类型
     *
     * @param psiType 类型对象
     * @return 参数Info对象
     */
    private static TypeInfo resolveType(PsiType psiType) {
        TypeInfo typeInfo = new TypeInfo();

        if (Objects.isNull(psiType)) {
            typeInfo.setCategory(FieldTypeEnum.VOID.getName());
            typeInfo.setSimpleName(FieldTypeEnum.VOID.getName());
            typeInfo.setFullName(FieldTypeEnum.VOID.getName());
            return typeInfo;
        }

        // 泛型参数
        if (psiType instanceof PsiTypeParameter) {
            PsiTypeParameter typeParameter = (PsiTypeParameter) psiType;
            typeInfo.setCategory(FieldTypeEnum.TYPE_PARAM.getName());
            typeInfo.setSimpleName(typeParameter.getName());
            typeInfo.setFullName(null);
            return typeInfo;
        }

        // 通配符
        if (psiType instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) psiType;
            typeInfo.setCategory(FieldTypeEnum.WILDCARD.getName());
            typeInfo.setSimpleName("?");
            typeInfo.setFullName(null);
            // 解析上下界
            PsiType typeBound = wildcardType.getBound();
            if (Objects.nonNull(typeBound)) {
                typeInfo.getGenericTypes().add(resolveType(typeBound));
            }
            return typeInfo;
        }

        // 基本类型
        if (psiType instanceof PsiPrimitiveType) {
            PsiPrimitiveType primitiveType = (PsiPrimitiveType) psiType;
            typeInfo.setCategory(FieldTypeEnum.BASIC.getName());
            typeInfo.setSimpleName(primitiveType.getPresentableText());
            typeInfo.setFullName(primitiveType.getCanonicalText());
            return typeInfo;
        }

        // 数组类型
        if (psiType instanceof PsiArrayType) {
            PsiArrayType arrayType = (PsiArrayType) psiType;
            typeInfo.setCategory(FieldTypeEnum.ARRAY.getName());
            typeInfo.setArray(true);
            typeInfo.setSimpleName(FieldTypeEnum.ARRAY.getName());
            typeInfo.setFullName(psiType.getCanonicalText());
            typeInfo.getGenericTypes().add(resolveType(arrayType.getComponentType()));
            return typeInfo;
        }

        if (psiType instanceof PsiClassType) {
            return resolvePsiClassType(typeInfo, ((PsiClassType) psiType));
        }

        // 其他未知类型
        typeInfo.setCategory(FieldTypeEnum.UNKNOWN.getName());
        typeInfo.setSimpleName(psiType.getPresentableText());
        typeInfo.setFullName(psiType.getCanonicalText());
        return typeInfo;
    }

    /**
     * 解析 PisClass 类型的对象
     * 包括: 类、接口、枚举、字符串、集合、Map、函数式接口
     *
     * @param typeInfo  类型信息对象
     * @param classType 被解析对象
     * @return 解析结果对象
     */
    private static TypeInfo resolvePsiClassType(TypeInfo typeInfo, PsiClassType classType) {
        PsiClass psiClass = classType.resolve();
        if (Objects.isNull(psiClass)) {
            typeInfo.setCategory(FieldTypeEnum.UNKNOWN.getName());
            typeInfo.setSimpleName(classType.getPresentableText());
            typeInfo.setFullName(classType.getCanonicalText());
            return typeInfo;
        }

        typeInfo.setSimpleName(Optional.ofNullable(psiClass.getName()).orElse(FieldTypeEnum.UNKNOWN.getName()));
        typeInfo.setFullName(psiClass.getQualifiedName());

        if (PsiMethodUtil.isJavaLangString(psiClass)) {
            typeInfo.setCategory(FieldTypeEnum.STRING.getName());
        } else if (PsiMethodUtil.isEnum(psiClass)) {
            typeInfo.setCategory(FieldTypeEnum.ENUM.getName());
        } else if (PsiMethodUtil.isFunctionalInterface(psiClass)) {
            typeInfo.setCategory(FieldTypeEnum.FUNCTIONAL.getName());
            resolveGenericTypes(classType, typeInfo);
            return typeInfo;
        } else if (PsiMethodUtil.isNormalInterface(psiClass)) {
            typeInfo.setCategory(FieldTypeEnum.INTERFACE.getName());
            if (PsiMethodUtil.isCollection(psiClass)) {
                typeInfo.setCategory(FieldTypeEnum.LIST.getName());
            } else if (PsiMethodUtil.isMap(psiClass)) {
                typeInfo.setCategory(FieldTypeEnum.MAP.getName());
            }
            resolveGenericTypes(classType, typeInfo);
            typeInfo.setFields(resolveClassFields(psiClass));
        } else {
            typeInfo.setCategory(FieldTypeEnum.CLASS.getName());
            resolveGenericTypes(classType, typeInfo);
            typeInfo.setFields(resolveClassFields(psiClass));
        }
        return typeInfo;
    }

    /**
     * 解析泛型参数
     *
     * @param psiClassType 类对象
     * @param typeInfo     参数类型对象
     */
    private static void resolveGenericTypes(PsiClassType psiClassType, TypeInfo typeInfo) {
        PsiType[] parameters = psiClassType.getParameters();
        for (PsiType parameter : parameters) {
            typeInfo.getGenericTypes().add(resolveType(parameter));
        }
    }

    /**
     * 解析类/接口的字段信息
     *
     * @param psiClass 被解析的类对象
     * @return 解析后的字段对象集合
     */
    private static List<FieldInfo> resolveClassFields(PsiClass psiClass) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        if (Objects.isNull(psiClass) || psiClass instanceof PsiCompiledElement) {
            return fieldInfoList;
        }

        // 解析非静态字段
        for (PsiField psiField : psiClass.getFields()) {
            if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.setFiledName(Optional.of(psiField.getName())
                    .orElse(FieldTypeEnum.UNKNOWN.getName()));
            fieldInfo.setFieldType(resolveType(psiField.getType()));
            fieldInfoList.add(fieldInfo);
        }

        // 解析类的父类字段
        PsiClass superClass = psiClass.getSuperClass();
        if (Objects.nonNull(superClass) && StringUtils.equals(OBJECT_STRING, superClass.getQualifiedName())) {
            fieldInfoList.addAll(resolveClassFields(superClass));
        }

        // 解析接口的父类字段
        for (PsiClass anInterface : psiClass.getInterfaces()) {
            fieldInfoList.addAll(resolveClassFields(anInterface));
        }

        return fieldInfoList;
    }
}
