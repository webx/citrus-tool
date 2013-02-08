package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import static com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.dom.DomDocumentUtil.*;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;

public abstract class AbstractNamespaceItemDetailsPage<T extends NamespaceItem> extends AbstractTreeItemDetailsPage<T> {
    protected CheckboxTableViewer schemasTable;

    protected final void createSchemasTable() {
        // Schemas
        toolkit.createLabel(client, "Schemas");
        Table table = new Table(client, SWT.CHECK); // table with no border
        toolkit.adapt(table);
        table.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));

        schemasTable = new CheckboxTableViewer(table);

        schemasTable.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                return ((NamespaceItem) inputElement).getSchemas().toArray();
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

                return location != null && location.endsWith(schema.getName());
            }

            public boolean isGrayed(Object element) {
                return false;
            }
        });

        schemasTable.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                updateNamespaceDefinitionLocation(config, (Schema) event.getElement(), event.getChecked());
                config.refreshNamespacesPage();
            }
        });
    }

    @Override
    protected void update() {
        if (schemasTable != null) {
            schemasTable.setInput(item);
        }
    }
}
