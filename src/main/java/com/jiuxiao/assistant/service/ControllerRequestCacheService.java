package com.jiuxiao.assistant.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.jiuxiao.assistant.dto.controller.RequestMappingInfo;
import com.jiuxiao.assistant.util.AnnoUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 控制器请求缓存服务类，用于管理和缓存Spring MVC的Controller类中的请求映射信息
 * 该服务实现了BulkFileListener和ModuleRootListener接口，用于监听文件变更和模块根变更事件
 *
 * @author 悟道九霄
 * @date 2026/4/18
 */
@Service(Service.Level.PROJECT)
public final class ControllerRequestCacheService implements BulkFileListener, ModuleRootListener {

    private final Project project;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    private static final String JAVA = "java";
    private static final String REQUEST_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String GET_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.PostMapping";
    private static final String PUT_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.PutMapping";
    private static final String DELETE_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.DeleteMapping";
    private static final String PATCH_MAPPING_FULL_NAME = "org.springframework.web.bind.annotation.PatchMapping";
    private static final String REST_CONTROLLER_FULL_NAME = "org.springframework.web.bind.annotation.RestController";
    private static final String CONTROLLER_FULL_NAME = "org.springframework.stereotype.Controller";
    private static final String RESPONSE_BODY_FULL_NAME = "org.springframework.web.bind.annotation.ResponseBody";
    private static final String VALUE_STRING = "value";
    private static final String METHOD_STRING = "method";

    private final List<RequestMappingInfo> requestMappingInfoList = new ArrayList<>();
    private final Map<String, PsiMethod> requestMethodMap = new ConcurrentHashMap<>();
    private final List<Runnable> initCallbacks = new ArrayList<>();

    /**
     * 构造函数，初始化服务并订阅文件变更事件
     *
     * @param project 当前项目实例
     */
    public ControllerRequestCacheService(Project project) {
        this.project = project;
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    /**
     * 初始化控制器请求映射
     */
    public void initControllerRequestMap() {
        if (initialized.get()) {
            return;
        }
        ensureInitialized();
    }

    /**
     * 刷新控制器请求映射
     */
    public void refreshControllerRequestMap() {
        initialized.set(false);
        ApplicationManager.getApplication().runReadAction(() -> {
            if (project.isDisposed()) {
                return;
            }
            synchronized (requestMappingInfoList) {
                requestMappingInfoList.clear();
                requestMethodMap.clear();
            }
            ensureInitialized();
        });
    }

    /**
     * 添加初始化回调
     *
     * @param callback 回调任务
     */
    public void addInitCallback(Runnable callback) {
        if (initialized.get()) {
            callback.run();
        } else {
            synchronized (initCallbacks) {
                initCallbacks.add(callback);
            }
        }
    }

    /**
     * 确保服务已初始化，如果未初始化则开始扫描
     */
    private void ensureInitialized() {
        if (initialized.get() || project.isDisposed()) {
            return;
        }

        if (isScanning.get()) {
            return;
        }

        // 直接执行扫描，不等待智能模式（因为 FilenameIndex 可以在非智能模式下工作）
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (initialized.get() || project.isDisposed()) {
                return;
            }

            if (!isScanning.compareAndSet(false, true)) {
                return;
            }

            try {
                // 等待索引就绪，但不要求完全智能模式
                com.intellij.openapi.project.DumbService.getInstance(project).waitForSmartMode();
                ApplicationManager.getApplication().runReadAction(() -> {
                    if (!initialized.get() && !project.isDisposed()) {
                        try {
                            scanProjectControllers();
                            initialized.set(true);
                            notifyInitComplete();
                        } catch (Exception e) {
                            com.intellij.openapi.diagnostic.Logger.getInstance(getClass()).warn("Failed to scan controllers", e);
                        }
                    }
                });
            } finally {
                isScanning.set(false);
            }
        });
    }

    /**
     * 通知初始化完成，执行所有回调
     */
    private void notifyInitComplete() {
        synchronized (initCallbacks) {
            for (Runnable callback : initCallbacks) {
                try {
                    callback.run();
                } catch (Exception e) {
                    com.intellij.openapi.diagnostic.Logger.getInstance(getClass()).warn("Callback execution failed", e);
                }
            }
            initCallbacks.clear();
        }
    }

    /**
     * 文件变更后的处理方法
     *
     * @param events 文件变更事件列表
     */
    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            for (VFileEvent event : events) {
                VirtualFile file = event.getFile();
                if (file == null) {
                    continue;
                }
                if (JAVA.equals(file.getExtension())) {
                    processJavaFileChange(file);
                }
            }
        });
    }

    /**
     * 处理Java文件变更
     *
     * @param file 变更的虚拟文件
     */
    private void processJavaFileChange(VirtualFile file) {
        ApplicationManager.getApplication().runReadAction(() -> {
            if (project.isDisposed()) {
                return;
            }
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (!(psiFile instanceof PsiJavaFile)) {
                return;
            }
            PsiJavaFile javaFile = (PsiJavaFile) psiFile;
            PsiClass[] classes = javaFile.getClasses();
            for (PsiClass psiClass : classes) {
                if (isControllerClass(psiClass)) {
                    removeOldMappingsForClass(psiClass);
                    scanControllerClass(psiClass);
                } else {
                    removeOldMappingsForClass(psiClass);
                }
            }
        });
    }

    /**
     * 移除指定类的旧映射信息
     *
     * @param psiClass PSI类对象
     */
    private void removeOldMappingsForClass(PsiClass psiClass) {
        String className = psiClass.getQualifiedName();
        if (className == null) {
            return;
        }
        synchronized (requestMappingInfoList) {
            requestMappingInfoList.removeIf(info -> info.getQualifiedName() != null && info.getQualifiedName().startsWith(className));
            requestMethodMap.entrySet().removeIf(entry -> {
                PsiMethod method = entry.getValue();
                if (method == null || !method.isValid()) {
                    return true;
                }
                PsiClass containingClass = method.getContainingClass();
                return containingClass == null || !className.equals(containingClass.getQualifiedName());
            });
        }
    }

    /**
     * 模块根变更处理方法
     *
     * @param event 模块根变更事件
     */
    @Override
    public void rootsChanged(@NotNull ModuleRootEvent event) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!project.isDisposed()) {
                initialized.set(false);
                refreshControllerRequestMap();
            }
        });
    }

    /**
     * 扫描项目中的所有控制器类
     */
    private void scanProjectControllers() {
        Collection<VirtualFile> javaVirtualFiles = FilenameIndex.getAllFilesByExt(
                project, JAVA, GlobalSearchScope.projectScope(project));

        for (VirtualFile virtualFile : javaVirtualFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile instanceof PsiJavaFile) {
                processJavaFile((PsiJavaFile) psiFile);
            }
        }
    }

    /**
     * 处理Java文件，扫描其中的控制器类
     *
     * @param javaFile Java PSI文件
     */
    private void processJavaFile(PsiJavaFile javaFile) {
        PsiClass[] classes = javaFile.getClasses();
        for (PsiClass psiClass : classes) {
            if (isControllerClass(psiClass)) {
                scanControllerClass(psiClass);
            }
        }
    }

    /**
     * 判断是否为控制器类
     *
     * @param psiClass PSI类对象
     * @return 如果是控制器类返回true，否则返回false
     */
    private boolean isControllerClass(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }

        PsiAnnotation restControllerAnno = psiClass.getAnnotation(REST_CONTROLLER_FULL_NAME);
        if (restControllerAnno != null) {
            return true;
        }

        PsiAnnotation controllerAnno = psiClass.getAnnotation(CONTROLLER_FULL_NAME);
        if (controllerAnno == null) {
            return false;
        }

        PsiAnnotation responseBodyAnno = psiClass.getAnnotation(RESPONSE_BODY_FULL_NAME);
        return responseBodyAnno != null;
    }

    /**
     * 扫描控制器类，提取其中的请求映射信息并存储到集合中
     *
     * @param psiClass 要扫描的控制器类（PsiClass对象）
     */
    private void scanControllerClass(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.getMethods();
        String classRequestMapping = getClassRequestMapping(psiClass);

        for (PsiMethod method : methods) {
            RequestMappingInfo info = extractRequestMappingInfo(method, classRequestMapping);
            if (info != null) {
                synchronized (requestMappingInfoList) {
                    requestMappingInfoList.add(info);
                    String key = info.getUrl() + "-" + info.getRequestMethod();
                    requestMethodMap.put(key, method);
                }
            }
        }
    }

    /**
     * 获取类级别的RequestMapping路径
     *
     * @param psiClass 要分析的PsiClass对象
     * @return 返回类级别@RequestMapping注解的value值，如果注解不存在或value为空则返回空字符串
     */
    private String getClassRequestMapping(PsiClass psiClass) {
        PsiAnnotation annotation = psiClass.getAnnotation(REQUEST_MAPPING_FULL_NAME);
        if (annotation == null) {
            return "";
        }
        String[] values = AnnoUtil.getAttributeValues(annotation, VALUE_STRING);
        if (ArrayUtil.isEmpty(values)) {
            return "";
        }
        return values[0];
    }

    /**
     * 从给定的PsiMethod中提取RequestMapping信息
     *
     * @param method       要分析的PsiMethod对象
     * @param classMapping 类级别的URL映射路径
     * @return 包含请求映射信息的RequestMappingInfo对象，如果没有找到请求注解则返回null
     */
    private RequestMappingInfo extractRequestMappingInfo(PsiMethod method, String classMapping) {
        PsiAnnotation requestAnnotation = findRequestAnnotation(method);
        if (requestAnnotation == null) {
            return null;
        }

        String methodMapping = getMethodMapping(requestAnnotation);
        String url = combineUrl(classMapping, methodMapping);
        String requestMethod = getRequestMethodType(requestAnnotation);

        PsiClass containingClass = method.getContainingClass();
        String className = containingClass != null ? containingClass.getName() : "";
        String qualifiedName = containingClass != null ? containingClass.getQualifiedName() : "";

        RequestMappingInfo info = new RequestMappingInfo();
        info.setUrl(url);
        info.setRequestMethod(requestMethod);
        info.setClassName(className);
        info.setMethodName(method.getName());
        info.setQualifiedName(qualifiedName + "." + method.getName());
        return info;
    }

    /**
     * 查找方法上的请求映射注解，该方法会按照以下顺序检查方法上的注解：
     * - @RequestMapping
     * - @GetMapping
     * - @PostMapping
     * - @PutMapping
     * - @DeleteMapping
     * - @PatchMapping
     *
     * @param method 要检查的PsiMethod对象
     * @return 找到的第一个匹配的请求映射注解，如果没有找到则返回null
     */
    private PsiAnnotation findRequestAnnotation(PsiMethod method) {
        PsiAnnotation requestMapping = method.getAnnotation(REQUEST_MAPPING_FULL_NAME);
        if (requestMapping != null) {
            return requestMapping;
        }
        PsiAnnotation getMapping = method.getAnnotation(GET_MAPPING_FULL_NAME);
        if (getMapping != null) {
            return getMapping;
        }
        PsiAnnotation postMapping = method.getAnnotation(POST_MAPPING_FULL_NAME);
        if (postMapping != null) {
            return postMapping;
        }
        PsiAnnotation putMapping = method.getAnnotation(PUT_MAPPING_FULL_NAME);
        if (putMapping != null) {
            return putMapping;
        }
        PsiAnnotation deleteMapping = method.getAnnotation(DELETE_MAPPING_FULL_NAME);
        if (deleteMapping != null) {
            return deleteMapping;
        }
        return method.getAnnotation(PATCH_MAPPING_FULL_NAME);
    }

    /**
     * 从给定的注解中提取方法映射路径
     *
     * @param annotation 要解析的Psi注解对象
     * @return 返回注解中的方法映射路径字符串，如果不存在则返回空字符串
     */
    private String getMethodMapping(PsiAnnotation annotation) {
        String[] values = AnnoUtil.getAttributeValues(annotation, VALUE_STRING);
        if (!ArrayUtil.isEmpty(values) && values[0] != null) {
            return values[0];
        }
        return "";
    }

    /**
     * 根据注解获取HTTP请求方法类型
     *
     * @param annotation PSI注解对象
     * @return HTTP请求方法类型字符串，包括"GET"、"POST"、"PUT"、"DELETE"、"PATCH"或"REQUEST"
     * 对于无法识别的注解或无法获取method属性的@RequestMapping注解，返回"REQUEST"
     */
    private String getRequestMethodType(PsiAnnotation annotation) {
        String qualifiedName = annotation.getQualifiedName();
        if (qualifiedName == null) {
            return "REQUEST";
        }
        switch (qualifiedName) {
            case GET_MAPPING_FULL_NAME:
                return "GET";
            case POST_MAPPING_FULL_NAME:
                return "POST";
            case PUT_MAPPING_FULL_NAME:
                return "PUT";
            case DELETE_MAPPING_FULL_NAME:
                return "DELETE";
            case PATCH_MAPPING_FULL_NAME:
                return "PATCH";
            default:
                // 对于 @RequestMapping，尝试从 method 属性获取
                if (REQUEST_MAPPING_FULL_NAME.equals(qualifiedName)) {
                    String[] methods = AnnoUtil.getAttributeValues(annotation, METHOD_STRING);
                    if (!ArrayUtil.isEmpty(methods) && methods[0] != null) {
                        return methods[0];
                    }
                }
                return "REQUEST";
        }
    }

    /**
     * 组合类级别的映射路径和方法级别的映射路径，生成完整的URL路径。
     * 处理路径拼接时的斜杠问题，确保最终URL以斜杠开头。
     *
     * @param classMapping  类级别的映射路径，可以为空
     * @param methodMapping 方法级别的映射路径，可以为空
     * @return 组合后的完整URL路径，始终以斜杠开头
     */
    private String combineUrl(String classMapping, String methodMapping) {
        String url = classMapping;
        if (StringUtils.isNotEmpty(methodMapping)) {
            if (StringUtils.isEmpty(classMapping)) {
                url = methodMapping;
            } else {
                if (!classMapping.endsWith("/") && !methodMapping.startsWith("/")) {
                    url = classMapping + "/" + methodMapping;
                } else if (classMapping.endsWith("/") && methodMapping.startsWith("/")) {
                    url = classMapping + methodMapping.substring(1);
                } else {
                    url = classMapping + methodMapping;
                }
            }
        }

        // 确保 URL 以 / 开头
        if (StringUtils.isNotEmpty(url) && !url.startsWith("/")) {
            url = "/" + url;
        }

        return url;
    }

    /**
     * 获取请求映射信息列表
     *
     * @return 包含所有请求映射信息的列表的副本
     */
    public List<RequestMappingInfo> getRequestMappingInfoList() {
        if (!initialized.get()) {
            ensureInitialized();
        }
        synchronized (requestMappingInfoList) {
            return new ArrayList<>(requestMappingInfoList);
        }
    }

    /**
     * 根据关键词搜索RequestMappingInfo列表
     *
     * @param keyword 搜索关键词，用于过滤URL路径
     * @return 匹配条件的RequestMappingInfo列表，如果关键词为空则返回所有RequestMappingInfo
     */
    public List<RequestMappingInfo> searchRequestMapping(String keyword) {
        if (!initialized.get()) {
            ensureInitialized();
        }
        if (StringUtils.isBlank(keyword)) {
            return getRequestMappingInfoList();
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        synchronized (requestMappingInfoList) {
            return requestMappingInfoList.stream()
                    .filter(info -> info.getUrl().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 根据请求映射信息查找对应的Psi方法
     *
     * @param info 请求映射信息，包含URL和请求方法
     * @return 返回找到的PsiMethod，如果未找到则返回null
     */
    public PsiMethod findPsiMethod(RequestMappingInfo info) {
        String key = info.getUrl() + "-" + info.getRequestMethod();
        return requestMethodMap.get(key);
    }

    /**
     * 导航到指定RequestMappingInfo对应的方法实现
     *
     * @param info RequestMappingInfo对象，包含请求映射信息
     */
    public void navigateToMethod(RequestMappingInfo info) {
        PsiMethod method = findPsiMethod(info);
        if (method != null && method.isValid()) {
            method.navigate(true);
        }
    }
}