package com.jiuxiao.assistant.provider;

import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.jiuxiao.assistant.service.MapperXmlCacheService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * JavaMethodLineMarkerProvider 类实现了 LineMarkerProvider 接口，
 * 用于在 Java 代码中的 Mapper 方法旁显示导航图标，以便快速跳转到对应的 XML 映射文件。
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public class JavaMethodLineMarkerProvider implements LineMarkerProvider {

    private static final Icon ICON = AllIcons.Gutter.ImplementedMethod;

    /**
     * 获取行标记信息，用于在编辑器中显示导航图标
     *
     * @param element 当前 PSI 元素
     * @return 相关项行标记信息，如果不满足条件则返回 null
     */
    @Override
    @Nullable
    public RelatedItemLineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier)) {
            return null;
        }

        PsiElement parent = element.getParent();
        if (!(parent instanceof PsiMethod)) {
            return null;
        }

        PsiMethod method = (PsiMethod) parent;

        if (MapperXmlCacheService.isMapperMethod(method)) {
            MapperXmlCacheService service = method.getProject().getService(MapperXmlCacheService.class);
            if (service != null) {
                // 先尝试建立映射（如果还没建立）
                service.tryAddMethodMappingImmediately(method);

                // 再查找映射
                PsiElement xmlTarget = service.findXmlTarget(method);
                if (xmlTarget != null && xmlTarget.isValid()) {
                    return NavigationGutterIconBuilder.create(ICON)
                            .setTooltipText("Navigate to MyBatis XML")
                            .setTarget(xmlTarget)
                            .createLineMarkerInfo(element);
                }
            }
        }

        return null;
    }
}