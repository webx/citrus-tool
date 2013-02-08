package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.util.Set;

import org.eclipse.ui.forms.widgets.FormText;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ConfigurationPointSchemaSourceInfo;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.util.StringUtil;

public class ConfigurationPointItemDetailsPage extends AbstractNamespaceItemDetailsPage<ConfigurationPointItem> {
    private FormText namespaceText;
    private FormText sourceText;

    @Override
    protected void initSection() {
        section.setText("Configuration Point Schema");
        section.setDescription("The schema defined in [CLASSPATH]/META-INF/spring.configuration-points");

        // Namespace
        toolkit.createLabel(client, "Namespace");
        namespaceText = toolkit.createFormText(client, false);

        // Sources
        toolkit.createLabel(client, "Defined in");
        sourceText = toolkit.createFormText(client, false);

        createSchemasTable();
    }

    @Override
    protected void update() {
        namespaceText.setText(item.getNamespace(), false, true);

        Set<String> sources = createLinkedHashSet();
        Set<Schema> schemas = item.getSchemas();

        for (Schema schema : schemas) {
            ConfigurationPointSchemaSourceInfo sourceInfo = (ConfigurationPointSchemaSourceInfo) schema;
            Resource resource = (Resource) sourceInfo.getParent().getSource();

            try {
                sources.add(resource.getURL().toExternalForm());
            } catch (IOException ignored) {
            }
        }

        sourceText.setText(StringUtil.join(sources, "\n\n"), false, true);

        super.update();
    }
}
