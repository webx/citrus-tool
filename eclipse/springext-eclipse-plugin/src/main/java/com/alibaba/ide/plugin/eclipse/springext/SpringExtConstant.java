package com.alibaba.ide.plugin.eclipse.springext;

import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;

@SuppressWarnings("restriction")
public interface SpringExtConstant {
    String PLUGIN_ID = "springext-eclipse-plugin"; //$NON-NLS-1$
    String URL_PROTOCOL = "springext";
    String URL_PREFIX = "http://localhost:8080/schema/";
    String XML_EDITOR_ID = XMLMultiPageEditorPart.class.getName();
    String SPRING_BEANS_NS = "http://www.springframework.org/schema/beans";
    String[] SUPPORTED_CONTENT_TYPES = { "com.alibaba.ide.plugin.eclipse.springext.configFile",
            "com.alibaba.ide.plugin.eclipse.springext.configurationPointDefinitionFile",
            "com.alibaba.ide.plugin.eclipse.springext.contributionDefinitionFile",
            "com.alibaba.ide.plugin.eclipse.springext.springPluggableDefinitionFile" };
}
