package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.detail;

import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;

public class SpringNamespaceHandlerDetailsPage extends AbstractNamespaceItemDetailsPage<SpringPluggableItem> {
    private FormText prefixText;
    private FormText namespaceText;

    @Override
    protected void initSection() {
        section.setText("Spring Namespace Handler");
        section.setDescription("The namespace is defined in [CLASSPATH]/spring.handlers");

        // Prefix
        toolkit.createLabel(client, "Prefix");
        prefixText = toolkit.createFormText(client, false);
        prefixText.setLayoutData(new TableWrapData());

        // Namespace
        toolkit.createLabel(client, "Namespace");
        namespaceText = toolkit.createFormText(client, false);
        namespaceText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
    }

    @Override
    protected void update() {
        String prefix = config.getNamespaceDefinitions().getPrefix(item.getNamespace(), config.getSchemas());

        // prefix
        prefixText.setText(prefix, false, false);

        // namespace
        namespaceText.setText(item.getNamespace(), false, false);
        super.update();
    }
}
