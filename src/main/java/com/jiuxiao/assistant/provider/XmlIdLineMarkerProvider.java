package com.jiuxiao.assistant.provider;

import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.jiuxiao.assistant.service.MapperXmlCacheService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Optional;

/**
 * XmlIdLineMarkerProvider 类实现了 LineMarkerProvider 接口，用于在 XML 文件中的 id 属性旁边显示导航标记，
 * 这些标记可以导航到对应的 Mapper 方法。这个类主要用于 MyBatis Mapper XML 文件与 Java 接口方法之间的导航。
 *
 * @author 悟道九霄
 * @date 2026-04-18
 */
public class XmlIdLineMarkerProvider implements LineMarkerProvider {

    private static final String ID = "id";
    private static final Icon ICON = AllIcons.Gutter.ImplementingMethod;

    /**
     * 获取行标记信息的方法，这是 LineMarkerProvider 接口的主要实现方法
     *
     * @param element 当前的 PSI 元素
     * @return RelatedItemLineMarkerInfo<?> 如果满足条件则返回标记信息，否则返回 null
     */
    @Override
    @Nullable
    public RelatedItemLineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // 验证XML token和属性
        Optional<XmlTag> xmlTagOpt = Optional.of(element)
                .filter(XmlTokenImpl.class::isInstance)
                .map(XmlTokenImpl.class::cast)
                .filter(token -> token.getTokenType() == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .map(XmlTokenImpl::getParent)
                .map(PsiElement::getParent)
                .filter(com.intellij.psi.xml.XmlAttribute.class::isInstance)
                .map(com.intellij.psi.xml.XmlAttribute.class::cast)
                .filter(attr -> ID.equals(attr.getName()))
                .map(com.intellij.psi.xml.XmlAttribute::getParent)
                .map(XmlTag.class::cast)
                .filter(MapperXmlCacheService::isXmlSQLTag);

        if (xmlTagOpt.isEmpty()) {
            return null;
        }

        XmlTag xmlTag = xmlTagOpt.get();
        Optional<MapperXmlCacheService> serviceOpt = Optional.ofNullable(
                xmlTag.getProject().getService(MapperXmlCacheService.class)
        );

        if (serviceOpt.isEmpty()) {
            return null;
        }

        MapperXmlCacheService service = serviceOpt.get();
        service.tryAddXmlMappingImmediately(xmlTag);

        // 查找并验证目标方法
        return Optional.ofNullable(service.findMapperTarget(xmlTag))
                .filter(PsiMethod.class::isInstance)
                .filter(PsiElement::isValid)
                .map(PsiMethod.class::cast)
                .map(PsiMethod::getNameIdentifier)
                .map(targetElement -> NavigationGutterIconBuilder.create(ICON)
                        .setTooltipText("Navigate to Mapper method")
                        .setTarget(targetElement)
                        .createLineMarkerInfo(element))
                .orElse(null);
    }

}