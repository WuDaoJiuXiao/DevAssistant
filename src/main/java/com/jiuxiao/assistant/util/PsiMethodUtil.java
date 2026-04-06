package com.jiuxiao.assistant.util;

import com.alibaba.fastjson2.JSON;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.jiuxiao.assistant.dto.extract.MethodInfo;
import com.jiuxiao.assistant.dto.mock.RequestInfo;
import com.jiuxiao.assistant.enums.RequestMethodEnum;
import com.jiuxiao.assistant.generator.JsonGenerator;
import com.jiuxiao.assistant.resolver.MethodInfoResolver;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Psi方法通用工具类
 */
public class PsiMethodUtil {

    private static final String COLLECTION_STRING = "java.util.Collection";

    private static final String MAP_STRING = "java.util.Map";

    private static final String LANG_STRING = "java.lang.String";

    private static final String REQUEST_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String GET_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.PostMapping";
    private static final String PUT_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.PutMapping";
    private static final String DELETE_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.DeleteMapping";
    private static final String PATCH_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.PatchMapping";
    private static final String REST_CONTROLLER_FULL_NAME = "org.springframework.web.bind.annotation.RestController";
    private static final String CONTROLLER_FULL_NAME = "org.springframework.stereotype.Controller";
    private static final String RESPONSE_BODY_FULL_NAME = "org.springframework.web.bind.annotation.ResponseBody";

    private static final List<String> GET_METHOD_LIST = List.of("GET");
    private static final List<String> POST_METHOD_LIST = List.of("POST");
    private static final List<String> PUT_METHOD_LIST = List.of("PUT");
    private static final List<String> DELETE_METHOD_LIST = List.of("DELETE");
    private static final List<String> PATCH_METHOD_LIST = List.of("PATCH");

    private static final String VALUE_STRING = "value";
    private static final String METHOD_STRING = "method";

    private static final Set<String> REQUEST_MAPPING_SET = new HashSet<>(List.of(
            REQUEST_MAPPING_FULL_NAME, GET_MAPPING_FULL_NAME, POST_MAPPING_FULL_NAME,
            PUT_MAPPING_FULL_NAME, DELETE_MAPPING_FULL_NAME, PATCH_MAPPING_FULL_NAME
    ));

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

    /**
     * 判断是否为请求方法
     *
     * @param method 方法对象
     * @return 是否为请求方法
     */
    public static boolean isRequestControlMethod(PsiMethod method) {
        if (Objects.isNull(method)) {
            return false;
        }

        boolean inRequestClass = isInRequestClass(method);
        boolean annotationByRequest = isAnnotationByRequest(method);
        return inRequestClass && annotationByRequest;
    }

    /**
     * 判断当前方法所属的类，是否为请求类
     *
     * @param method 方法对象
     * @return 是否在目标类中
     */
    private static boolean isInRequestClass(PsiMethod method) {
        if (Objects.isNull(method)) {
            return false;
        }

        PsiClass psiClass = method.getContainingClass();
        if (Objects.isNull(psiClass)) {
            return false;
        }

        PsiAnnotation restControllerAnno = psiClass.getAnnotation(REST_CONTROLLER_FULL_NAME);
        if (Objects.nonNull(restControllerAnno)) {
            return true;
        }

        PsiAnnotation responseBodyAnno = psiClass.getAnnotation(RESPONSE_BODY_FULL_NAME);
        PsiAnnotation controllerAnno = psiClass.getAnnotation(CONTROLLER_FULL_NAME);
        return Objects.nonNull(responseBodyAnno) && Objects.nonNull(controllerAnno);
    }

    /**
     * 判断当前方法是否被常用的请求注解所标注
     *
     * @param method 方法对象
     * @return 是否被请求注解标注
     */
    private static boolean isAnnotationByRequest(PsiMethod method) {
        if (Objects.isNull(method)) {
            return false;
        }

        PsiAnnotation requestMappingAnno = method.getAnnotation(REQUEST_MAPPING_FULL_NAME);
        PsiAnnotation getMappingAnno = method.getAnnotation(GET_MAPPING_FULL_NAME);
        PsiAnnotation postMappingAnno = method.getAnnotation(POST_MAPPING_FULL_NAME);
        PsiAnnotation putMappingAnno = method.getAnnotation(PUT_MAPPING_FULL_NAME);
        PsiAnnotation deleteMappingAnno = method.getAnnotation(DELETE_MAPPING_FULL_NAME);
        PsiAnnotation patchMappingAnno = method.getAnnotation(PATCH_MAPPING_FULL_NAME);

        return ObjectUtils.anyNotNull(requestMappingAnno, getMappingAnno, postMappingAnno,
                putMappingAnno, deleteMappingAnno, patchMappingAnno
        );
    }

    /**
     * 获取请求方法的请求信息
     *
     * @param method 方法对象
     * @return 请求信息包装类
     */
    public static RequestInfo getMethodRequestInfo(PsiMethod method) {
        RequestInfo requestInfo = new RequestInfo();
        if (Objects.isNull(method)) {
            return requestInfo;
        }

        String requestUrl = Optional.ofNullable(getRequestUrl(method)).orElse(StringUtils.EMPTY);
        String requestMethod = Optional.ofNullable(getRequestMethod(method)).orElse(StringUtils.EMPTY);
        String requestBody = StringUtils.EMPTY;
        MethodInfo methodInfo = MethodInfoResolver.resolveMethodInfo(method);
        if (Objects.nonNull(methodInfo)) {
            requestBody = JsonGenerator.generateRequestBody(methodInfo, false);
        }

        requestInfo.setUrl(requestUrl);
        requestInfo.setRequestMethod(requestMethod.toUpperCase(Locale.ROOT));
        requestInfo.setRequestBody(JSON.parse(requestBody));
        return requestInfo;
    }

    /**
     * 获取请求类 @RequestMapping 注解的 value
     *
     * @param psiClass 类对象
     * @return 注解的值
     */
    private static String getRequestClassValue(PsiClass psiClass) {
        String emptyValue = StringUtils.EMPTY;
        if (Objects.isNull(psiClass)) {
            return emptyValue;
        }

        PsiAnnotation annotation = psiClass.getAnnotation(REQUEST_MAPPING_FULL_NAME);
        if (Objects.isNull(annotation)) {
            return emptyValue;
        }

        String[] attributeValues = AnnoUtil.getAttributeValues(annotation, PsiMethodUtil.VALUE_STRING);
        if (ArrayUtil.isEmpty(attributeValues)) {
            return emptyValue;
        }

        return attributeValues[0];
    }

    /**
     * 获取请求方法 @RequestMapping 注解的 value
     *
     * @param method 方法对象
     * @return 注解的值
     */
    private static String getRequestMethodValue(PsiMethod method) {
        String emptyValue = StringUtils.EMPTY;
        if (Objects.isNull(method)) {
            return emptyValue;
        }

        PsiAnnotation[] annotations = method.getAnnotations();
        if (ArrayUtil.isEmpty(annotations)) {
            return emptyValue;
        }

        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (REQUEST_MAPPING_SET.contains(qualifiedName)) {
                String[] attributeValues = AnnoUtil.getAttributeValues(annotation, PsiMethodUtil.VALUE_STRING);
                if (ArrayUtil.isEmpty(attributeValues)) {
                    return emptyValue;
                }
                return attributeValues[0];
            }
        }
        return emptyValue;
    }

    /**
     * 获取方法的请求方式
     *
     * @param method 方法对象
     * @return 请求方式名称
     */
    private static String getRequestMethod(PsiMethod method) {
        if (Objects.isNull(method)) {
            return StringUtils.EMPTY;
        }

        PsiAnnotation[] annotations = method.getAnnotations();
        if (ArrayUtil.isEmpty(annotations)) {
            return StringUtils.EMPTY;
        }

        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (Objects.equals(qualifiedName, REQUEST_MAPPING_FULL_NAME)) {
                String[] attributeValues = AnnoUtil.getAttributeValues(annotation, METHOD_STRING);
                return attributeValues.length > 0 ? attributeValues[0] : StringUtils.EMPTY;
            } else if (Objects.equals(qualifiedName, GET_MAPPING_FULL_NAME)) {
                return RequestMethodEnum.GET.getMethod();
            } else if (Objects.equals(qualifiedName, POST_MAPPING_FULL_NAME)) {
                return RequestMethodEnum.POST.getMethod();
            } else if (Objects.equals(qualifiedName, PUT_MAPPING_FULL_NAME)) {
                return RequestMethodEnum.PUT.getMethod();
            } else if (Objects.equals(qualifiedName, DELETE_MAPPING_FULL_NAME)) {
                return RequestMethodEnum.DELETE.getMethod();
            } else if (Objects.equals(qualifiedName, PATCH_MAPPING_FULL_NAME)) {
                return RequestMethodEnum.PATCH.getMethod();
            }
        }

        return StringUtils.EMPTY;
    }

    /**
     * 获取完整的请求URL路径
     *
     * @param psiMethod 方法对象
     * @return 请求URL路径
     */
    private static String getRequestUrl(PsiMethod psiMethod) {
        if (Objects.isNull(psiMethod)) {
            return StringUtils.EMPTY;
        }

        PsiClass containingClass = psiMethod.getContainingClass();
        if (Objects.isNull(containingClass)) {
            return StringUtils.EMPTY;
        }

        String classValue = getRequestClassValue(containingClass);
        String methodValue = getRequestMethodValue(psiMethod);
        return classValue + methodValue;
    }
}
