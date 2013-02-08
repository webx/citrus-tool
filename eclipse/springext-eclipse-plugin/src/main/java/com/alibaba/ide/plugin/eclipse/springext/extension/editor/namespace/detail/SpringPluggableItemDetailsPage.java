package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.citrus.util.StringUtil;

public class SpringPluggableItemDetailsPage extends AbstractTreeItemDetailsPage<SpringPluggableItem> {
    private FormText namespaceText;
    private FormText sourceText;
    private TableViewer schemasTable;

    @Override
    protected void initSection() {
        section.setText("Spring Pluggable Schema");
        section.setDescription("The schema defined in [CLASSPATH]/META-INF/spring.schemas");

        // Namespace
        toolkit.createLabel(client, "Namespace");
        namespaceText = toolkit.createFormText(client, false);

        // Sources
        toolkit.createLabel(client, "Source");
        sourceText = toolkit.createFormText(client, false);

        // Schemas
        toolkit.createLabel(client, "Schemas");
        Table table = toolkit.createTable(client, SWT.CHECK);
        table.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));

        schemasTable = new TableViewer(table);
    }

    @Override
    protected void update() {
        namespaceText.setText(item.getNamespace(), false, true);

        Set<String> sources = createLinkedHashSet();
        Set<Schema> schemas = item.getSchemas();

        for (Schema schema : schemas) {
            SpringPluggableSchemaSourceInfo sourceInfo = (SpringPluggableSchemaSourceInfo) schema;
            Resource resource = (Resource) sourceInfo.getParent().getSource();

            try {
                sources.add(resource.getURL().toExternalForm());
            } catch (IOException ignored) {
            }
        }

        sourceText.setText(StringUtil.join(sources, "\n\n"), false, true);
    }
}
