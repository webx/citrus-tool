package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import org.eclipse.ui.forms.widgets.FormText;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;

public class SpringNamespaceHandlerDetailsPage extends AbstractNamespaceItemDetailsPage<SpringPluggableItem> {
    private FormText namespaceText;

    @Override
    protected void initSection() {
        section.setText("Spring Namespace Handler");
        section.setDescription("The namespace is defined in [CLASSPATH]/spring.handlers");

        // Namespace
        toolkit.createLabel(client, "Namespace");
        namespaceText = toolkit.createFormText(client, false);
    }

    @Override
    protected void update() {
        namespaceText.setText(item.getNamespace(), false, true);
        super.update();
    }
}
