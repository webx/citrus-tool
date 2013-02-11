package com.alibaba.ide.plugin.eclipse.springext;

import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;

@SuppressWarnings("restriction")
public interface SpringExtConstant {
    String PLUGIN_ID = "springext-eclipse-plugin"; //$NON-NLS-1$
    String URL_PROTOCOL = "springext";
    String URL_PREFIX = "http://localhost:8080/schema/";
    String XML_EDITOR_ID = XMLMultiPageEditorPart.class.getName();
    String SPRING_BEANS_NS = "http://www.springframework.org/schema/beans";
    String SPRINGEXT_CONFIG_CONTENT_TYPE = "com.alibaba.ide.plugin.eclipse.springext.configFile";
}
