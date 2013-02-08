package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
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
    private CheckboxTableViewer schemasTable;

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
        Table table = new Table(client, SWT.CHECK); // table with no border
        toolkit.adapt(table);
        table.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));

        schemasTable = new CheckboxTableViewer(table);

        schemasTable.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                return ((SpringPluggableItem) inputElement).getSchemas().toArray();
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            public void dispose() {
            }
        });

        schemasTable.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Schema) element).getName();
            }
        });

        schemasTable.setCheckStateProvider(new ICheckStateProvider() {
            public boolean isChecked(Object element) {
                Schema schema = (Schema) element;
                String location = config.getNamespaceDefinitions().getLocation(schema.getTargetNamespace());

                if (location == null) {
                    return schema == item.getSchemas().iterator().next();
                } else {
                    return location.endsWith(schema.getName());
                }
            }

            public boolean isGrayed(Object element) {
                return false;
            }
        });
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
        schemasTable.setInput(item);
    }
}
