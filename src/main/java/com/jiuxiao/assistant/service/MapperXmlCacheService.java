package com.jiuxiao.assistant.service;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
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
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.jiuxiao.assistant.util.CommonUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * MapperXmlCacheService 类，用于缓存和管理Mapper接口与XML文件之间的映射关系
 * 实现了BulkFileListener和ModuleRootListener接口，用于监听文件和模块变化
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
@Service(Service.Level.PROJECT)
public final class MapperXmlCacheService implements BulkFileListener, ModuleRootListener {

    private final Project project;
    private volatile boolean initialized = false;

    private static final Set<String> SQL_OPERATOR_SET = new HashSet<>(List.of(
            "org.apache.ibatis.annotations.Select", "org.apache.ibatis.annotations.Update",
            "org.apache.ibatis.annotations.Insert", "org.apache.ibatis.annotations.Delete"
    ));

    private static final Set<String> XML_TAG_SET = new HashSet<>(List.of("select", "insert", "update", "delete"));

    private static final String XML = "xml";
    private static final String JAVA = "java";
    private static final String MAPPER = "mapper";
    private static final String NAMESPACE = "namespace";
    private static final String ID = "id";
    private static final String POUND_STRING = "#";
    private static final String MAPPER_ANNOTATION_FULL_NAME = "org.apache.ibatis.annotations.Mapper";


    private final Map<String, XmlTag> MAPPER_TO_XML_MAP = new ConcurrentHashMap<>();
    private final Map<String, PsiMethod> XML_TO_MAPPER_MAP = new ConcurrentHashMap<>();
    private final Map<String, PsiMethod> pendingMethodMappings = new ConcurrentHashMap<>();
    private final Map<String, XmlTag> pendingXmlMappings = new ConcurrentHashMap<>();

    /**
     * 构造函数，初始化服务并订阅文件变化事件
     *
     * @param project 当前项目实例
     */
    public MapperXmlCacheService(Project project) {
        this.project = project;
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    /**
     * 文件变化后的处理方法
     *
     * @param events 文件事件列表
     */
    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) return;

            for (VFileEvent event : events) {
                VirtualFile file = event.getFile();
                if (file == null) continue;

                String extension = file.getExtension();
                if (XML.equals(extension)) {
                    String fileName = file.getName();
                    if (fileName.contains(MAPPER) || fileName.endsWith("Mapper.xml")) {
                        processXmlFileChange(file);
                    }
                } else if (JAVA.equals(extension)) {
                    processJavaFileChange(file);
                }
            }
        });
    }

    /**
     * 处理Java文件变化
     *
     * @param file 变化的虚拟文件
     */
    private void processJavaFileChange(VirtualFile file) {
        ApplicationManager.getApplication().runReadAction(() -> {
            if (project.isDisposed()) return;

            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (!(psiFile instanceof PsiJavaFile)) return;

            PsiJavaFile javaFile = (PsiJavaFile) psiFile;
            for (PsiClass psiClass : javaFile.getClasses()) {
                if (psiClass.isInterface() && isMapperInterface(psiClass)) {
                    for (PsiMethod method : psiClass.getMethods()) {
                        if (isMapperMethod(method)) {
                            tryAddMethodMappingImmediately(method);
                        }
                    }
                }
            }
        });
    }

    /**
     * 处理XML文件变化
     *
     * @param file 变化的虚拟文件
     */
    private void processXmlFileChange(VirtualFile file) {
        ApplicationManager.getApplication().runReadAction(() -> {
            if (project.isDisposed()) return;

            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (!(psiFile instanceof XmlFile)) return;

            XmlFile xmlFile = (XmlFile) psiFile;
            XmlTag rootTag = xmlFile.getRootTag();
            if (rootTag == null || !MAPPER.equals(rootTag.getName())) return;

            String namespace = rootTag.getAttributeValue(NAMESPACE);
            if (StringUtils.isBlank(namespace)) return;

            // 处理所有 SQL 标签
            for (XmlTag subTag : rootTag.getSubTags()) {
                if (isXmlSQLTag(subTag)) {
                    tryAddXmlMappingImmediately(subTag);
                }
            }
        });
    }

    /**
     * 模块根变化时的处理方法
     *
     * @param event 模块根事件
     */
    @Override
    public void rootsChanged(@NotNull ModuleRootEvent event) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!project.isDisposed()) {
                refreshAffectedFiles();
            }
        });
    }

    /**
     * 尝试立即添加方法映射
     *
     * @param method Mapper方法
     */
    public void tryAddMethodMappingImmediately(PsiMethod method) {
        if (!isMapperMethod(method)) return;

        ApplicationManager.getApplication().runReadAction(() -> {
            if (project.isDisposed()) return;

            PsiClass containingClass = method.getContainingClass();
            if (containingClass == null) return;

            String namespace = containingClass.getQualifiedName();
            if (StringUtils.isBlank(namespace)) return;

            String methodName = method.getName();
            String methodKey = CommonUtil.buildMethodKey(containingClass, methodName);

            // 先检查是否已经有映射
            if (MAPPER_TO_XML_MAP.containsKey(methodKey)) {
                return;
            }

            // 推测性查找对应的 XML
            String expectedXmlName = Objects.requireNonNull(containingClass.getName())
                    .replace("Mapper", "") + "Mapper.xml";
            PsiFile[] xmlFiles = FilenameIndex.getFilesByName(
                    project,
                    expectedXmlName,
                    GlobalSearchScope.projectScope(project)
            );

            boolean found = false;
            for (PsiFile xmlFile : xmlFiles) {
                if (xmlFile instanceof XmlFile) {
                    XmlFile xml = (XmlFile) xmlFile;
                    XmlTag rootTag = xml.getRootTag();
                    if (rootTag != null && namespace.equals(rootTag.getAttributeValue(NAMESPACE))) {
                        // 查找对应的 SQL 标签（即使 id 属性还不存在，也尝试建立映射）
                        XmlTag sqlTag = findXmlTagById(rootTag, methodName);
                        if (sqlTag != null) {
                            // 找到完整的映射
                            String xmlKey = CommonUtil.buildXmlTagKey(namespace, methodName);
                            MAPPER_TO_XML_MAP.put(methodKey, sqlTag);
                            XML_TO_MAPPER_MAP.put(xmlKey, method);
                            found = true;

                            // 立即刷新 LineMarker
                            refreshLineMarkerForMethod(method);
                            break;
                        } else {
                            // 推测性映射：XML 文件存在但没有对应的标签，先记录下来
                            pendingMethodMappings.put(methodKey, method);
                        }
                    }
                }
            }

            if (!found && !pendingMethodMappings.containsKey(methodKey)) {
                // 没有找到对应的 XML 文件，也记录下来等待后续创建
                pendingMethodMappings.put(methodKey, method);
            }
        });
    }

    /**
     * 立即添加 XML 映射（推测性映射）
     *
     * @param xmlTag XML标签
     */
    public void tryAddXmlMappingImmediately(XmlTag xmlTag) {
        if (!isXmlSQLTag(xmlTag)) return;

        ApplicationManager.getApplication().runReadAction(() -> {
            if (project.isDisposed()) return;

            XmlTag rootTag = xmlTag.getParentTag();
            if (rootTag == null) return;

            String namespace = rootTag.getAttributeValue(NAMESPACE);
            String sqlId = xmlTag.getAttributeValue(ID);

            if (StringUtils.isBlank(namespace) || StringUtils.isBlank(sqlId)) return;

            String xmlKey = CommonUtil.buildXmlTagKey(namespace, sqlId);

            // 先检查是否已经有映射
            if (XML_TO_MAPPER_MAP.containsKey(xmlKey)) {
                return;
            }

            // 查找对应的 Mapper 类
            PsiClass mapperClass = findMapperClass(namespace);
            if (mapperClass != null) {
                PsiMethod method = findMethodByName(mapperClass, sqlId);
                if (method != null && isMapperMethod(method)) {
                    // 找到完整的映射
                    String methodKey = CommonUtil.buildMethodKey(mapperClass, sqlId);
                    MAPPER_TO_XML_MAP.put(methodKey, xmlTag);
                    XML_TO_MAPPER_MAP.put(xmlKey, method);

                    // 从待处理映射中移除
                    pendingMethodMappings.remove(methodKey);
                    pendingXmlMappings.remove(xmlKey);

                    // 立即刷新 LineMarker
                    refreshLineMarkerForMethod(method);
                    refreshLineMarkerForXmlTag(xmlTag);
                } else {
                    // 推测性映射：Mapper 类存在但没有对应的方法，先记录下来
                    pendingXmlMappings.put(xmlKey, xmlTag);
                }
            } else {
                // 推测性映射：Mapper 类还不存在，先记录下来
                pendingXmlMappings.put(xmlKey, xmlTag);
            }
        });
    }

    /**
     * 处理待处理的映射（当新文件创建时调用）
     */
    private void processPendingMappings() {
        // 处理待处理的方法映射
        if (!pendingMethodMappings.isEmpty()) {
            new HashMap<>(pendingMethodMappings).forEach((methodKey, method) -> {
                if (method.isValid()) {
                    tryAddMethodMappingImmediately(method);
                } else {
                    pendingMethodMappings.remove(methodKey);
                }
            });
        }

        // 处理待处理的 XML 映射
        if (!pendingXmlMappings.isEmpty()) {
            new HashMap<>(pendingXmlMappings).forEach((xmlKey, xmlTag) -> {
                if (xmlTag.isValid()) {
                    tryAddXmlMappingImmediately(xmlTag);
                } else {
                    pendingXmlMappings.remove(xmlKey);
                }
            });
        }
    }

    /**
     * 刷新受影响的文件
     */
    private void refreshAffectedFiles() {
        processPendingMappings();

        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) return;
            DaemonCodeAnalyzer.getInstance(project).restart();
        });
    }

    /**
     * 刷新单个方法的 LineMarker
     *
     * @param method Psi方法
     */
    private void refreshLineMarkerForMethod(PsiMethod method) {
        if (method == null || !method.isValid()) return;

        PsiIdentifier identifier = method.getNameIdentifier();
        if (identifier == null) return;

        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) return;
            PsiFile containingFile = identifier.getContainingFile();
            if (containingFile != null && containingFile.isValid()) {
                DaemonCodeAnalyzer.getInstance(project).restart(containingFile);
            }
        });
    }

    /**
     * 刷新 XML 标签的 LineMarker
     *
     * @param xmlTag XML标签
     */
    private void refreshLineMarkerForXmlTag(XmlTag xmlTag) {
        if (xmlTag == null || !xmlTag.isValid()) return;

        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) return;
            PsiFile containingFile = xmlTag.getContainingFile();
            if (containingFile != null && containingFile.isValid()) {
                DaemonCodeAnalyzer.getInstance(project).restart(containingFile);
            }
        });
    }

    /**
     * 判断是否为Mapper接口层方法
     *
     * @param method Psi方法
     * @return 是否为Mapper方法
     */
    public static boolean isMapperMethod(PsiMethod method) {
        if (Objects.isNull(method)) return false;

        PsiClass containingClass = method.getContainingClass();
        if (BooleanUtils.isFalse(isMapperInterface(containingClass))) return false;

        PsiAnnotation[] annotations = method.getAnnotations();
        if (ArrayUtil.isEmpty(annotations)) return true;

        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (SQL_OPERATOR_SET.contains(qualifiedName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否为XML中的SQL节点
     *
     * @param xmlTag XML标签
     * @return 是否为SQL标签
     */
    public static boolean isXmlSQLTag(XmlTag xmlTag) {
        return XML_TAG_SET.contains(xmlTag.getName());
    }

    /**
     * 判断是否为Mapper接口
     *
     * @param psiClass Psi类
     * @return 是否为Mapper接口
     */
    private static boolean isMapperInterface(PsiClass psiClass) {
        if (Objects.isNull(psiClass)) {
            return false;
        }
        PsiAnnotation mapperAnno = psiClass.getAnnotation(MAPPER_ANNOTATION_FULL_NAME);
        return Objects.nonNull(mapperAnno);
    }

    /**
     * 初始化Mapper接口和XML的映射关系
     */
    public void initMapperXmlMap() {
        MAPPER_TO_XML_MAP.clear();
        XML_TO_MAPPER_MAP.clear();
        pendingMethodMappings.clear();
        pendingXmlMappings.clear();

        Map<String, PsiClass> mapperInterfaceMap = scanAllMapperInterfaces();
        Collection<VirtualFile> xmlFiles = FilenameIndex.getAllFilesByExt(project, XML, GlobalSearchScope.projectScope(project));

        for (VirtualFile xmlFile : xmlFiles) {
            parseXmlAndBuildMapping(xmlFile, mapperInterfaceMap);
        }

        initialized = true;
    }

    /**
     * 解析XML对象并建立到Mapper的映射关系
     *
     * @param virtualFile        虚拟文件
     * @param mapperInterfaceMap Mapper接口映射
     */
    private void parseXmlAndBuildMapping(VirtualFile virtualFile, Map<String, PsiClass> mapperInterfaceMap) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (!(psiFile instanceof XmlFile)) {
            return;
        }

        XmlFile xmlFile = (XmlFile) psiFile;
        XmlTag rootTag = xmlFile.getRootTag();
        if (Objects.isNull(rootTag) || ObjectUtils.notEqual(MAPPER, rootTag.getName())) return;

        String namespace = rootTag.getAttributeValue(NAMESPACE);
        if (StringUtils.isBlank(namespace)) return;

        PsiClass mapperClass = mapperInterfaceMap.get(namespace);
        if (Objects.isNull(mapperClass)) return;

        Map<String, PsiMethod> methodMap = new HashMap<>();
        for (PsiMethod method : mapperClass.getMethods()) {
            if (isMapperMethod(method)) {
                methodMap.put(method.getName(), method);
            }
        }

        for (XmlTag subTag : rootTag.getSubTags()) {
            if (!isXmlSQLTag(subTag)) continue;

            String sqlId = subTag.getAttributeValue(ID);
            if (StringUtils.isBlank(sqlId)) continue;

            PsiMethod method = methodMap.get(sqlId);
            if (Objects.isNull(method)) continue;

            String methodKey = CommonUtil.buildMethodKey(mapperClass, sqlId);
            String xmlKey = CommonUtil.buildXmlTagKey(namespace, sqlId);

            MAPPER_TO_XML_MAP.put(methodKey, subTag);
            XML_TO_MAPPER_MAP.put(xmlKey, method);
        }
    }

    /**
     * 扫描所有 Mapper接口
     *
     * @return Mapper接口映射
     */
    private Map<String, PsiClass> scanAllMapperInterfaces() {
        Map<String, PsiClass> result = new HashMap<>();
        Collection<VirtualFile> javaFiles = FilenameIndex.getAllFilesByExt(project, JAVA, GlobalSearchScope.projectScope(project));

        for (VirtualFile virtualFile : javaFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile javaFile = (PsiJavaFile) psiFile;
                for (PsiClass psiClass : javaFile.getClasses()) {
                    if (psiClass.isInterface() && isMapperInterface(psiClass)) {
                        String qualifiedName = psiClass.getQualifiedName();
                        if (StringUtils.isNotBlank(qualifiedName)) {
                            result.put(qualifiedName, psiClass);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 寻找要跳转的XML对象
     *
     * @param method Mapper方法
     * @return XML元素
     */
    public PsiElement findXmlTarget(PsiMethod method) {
        ensureInitialized();
        PsiClass containingClass = method.getContainingClass();
        if (Objects.isNull(containingClass)) return null;

        String key = containingClass.getQualifiedName() + POUND_STRING + method.getName();
        return MAPPER_TO_XML_MAP.get(key);
    }

    /**
     * 寻找要跳转的Mapper对象
     *
     * @param xmlTag XML标签
     * @return Mapper方法
     */
    public PsiElement findMapperTarget(XmlTag xmlTag) {
        ensureInitialized();
        XmlTag rootTag = xmlTag.getParentTag();
        if (Objects.isNull(rootTag)) return null;

        String namespace = rootTag.getAttributeValue(NAMESPACE);
        String sqlId = xmlTag.getAttributeValue(ID);
        if (StringUtils.isAnyBlank(namespace, sqlId)) return null;

        String key = namespace + POUND_STRING + sqlId;
        return XML_TO_MAPPER_MAP.get(key);
    }

    /**
     * 确保已初始化
     */
    private void ensureInitialized() {
        if (initialized && !project.isDisposed()) {
            return;
        }

        com.intellij.openapi.project.DumbService.getInstance(project).smartInvokeLater(() -> {
            if (initialized || project.isDisposed()) return;

            ApplicationManager.getApplication().runReadAction(() -> {
                if (!initialized && !project.isDisposed()) {
                    try {
                        initMapperXmlMap();
                        initialized = true;
                    } catch (Exception e) {
                        com.intellij.openapi.diagnostic.Logger.getInstance(getClass()).warn(e);
                    }
                }
            });
        });
    }

    /**
     * 辅助查找方法
     *
     * @param namespace 命名空间
     * @return Mapper类
     */
    private PsiClass findMapperClass(String namespace) {
        return JavaPsiFacade.getInstance(project).findClass(namespace, GlobalSearchScope.projectScope(project));
    }

    /**
     * 根据方法名查找方法
     *
     * @param psiClass   Psi类
     * @param methodName 方法名
     * @return Psi方法
     */
    private PsiMethod findMethodByName(PsiClass psiClass, String methodName) {
        for (PsiMethod method : psiClass.getMethods()) {
            if (methodName.equals(method.getName()) && isMapperMethod(method)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 根据ID查找XML标签
     *
     * @param rootTag 根标签
     * @param id      标签ID
     * @return XML标签
     */
    private XmlTag findXmlTagById(XmlTag rootTag, String id) {
        for (XmlTag tag : rootTag.getSubTags()) {
            if (isXmlSQLTag(tag) && id.equals(tag.getAttributeValue(ID))) {
                return tag;
            }
        }
        return null;
    }
}