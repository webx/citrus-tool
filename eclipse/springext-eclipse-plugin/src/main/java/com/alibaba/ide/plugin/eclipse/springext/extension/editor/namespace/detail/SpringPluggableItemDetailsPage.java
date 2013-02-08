package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.Set;

import org.eclipse.ui.forms.widgets.FormText;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.citrus.util.StringUtil;

public class SpringPluggableItemDetailsPage extends AbstractNamespaceItemDetailsPage<SpringPluggableItem> {
    private FormText namespaceText;
    private FormText sourceText;

    @Override
    protected void initSection() {
        section.setText("Spring Pluggable Schema");
        section.setDescription("The schema defined in [CLASSPATH]/META-INF/spring.schemas");

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
            SpringPluggableSchemaSourceInfo sourceInfo = (SpringPluggableSchemaSourceInfo) schema;
            sources.add(getSourceDesc(sourceInfo.getParent()));
        }

        sourceText.setText(StringUtil.join(sources, "\n\n"), false, true);

        super.update();
    }
}
