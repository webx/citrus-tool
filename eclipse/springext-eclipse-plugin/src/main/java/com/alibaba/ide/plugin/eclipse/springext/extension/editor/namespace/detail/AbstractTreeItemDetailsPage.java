package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.dom.DomDocumentUtil.*;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfig;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.NamespacesMasterPart;

public abstract class AbstractTreeItemDetailsPage<T extends NamespaceItem> implements IDetailsPage {
    protected IManagedForm form;
    protected FormToolkit toolkit;
    protected T item;
    protected SpringExtConfig config;
    protected Section section;
    protected Composite client;
    protected CheckboxTableViewer schemasTable;

    public void initialize(IManagedForm form) {
        this.form = form;
        this.toolkit = form.getToolkit();
        this.config = getNamespacesMasterPart().getConfig();
    }

    private NamespacesMasterPart getNamespacesMasterPart() {
        return assertNotNull(SpringExtConfig.getFormPart(NamespacesMasterPart.class, form),
                "no Namespaces master part found");
    }

    public final void createContents(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);

        // section
        section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        section.marginWidth = 10;
        section.marginHeight = 5;
        section.clientVerticalSpacing = 8;

        // 和master part的description对齐。
        section.descriptionVerticalSpacing = getNamespacesMasterPart().getSection().getTextClientHeightDifference();

        // section/client
        client = toolkit.createComposite(section);
        TableWrapLayout tlayout = new TableWrapLayout();
        tlayout.numColumns = 2;
        tlayout.horizontalSpacing = 20;
        tlayout.verticalSpacing = 20;
        client.setLayout(tlayout);

        section.setClient(client);

        initSection();
    }

    protected final void createSchemasTable() {
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

    protected abstract void initSection();

    public void dispose() {
    }

    public boolean isDirty() {
        return false;
    }

    public void commit(boolean onSave) {
    }

    public boolean setFormInput(Object input) {
        return false;
    }

    public void setFocus() {
    }

    public boolean isStale() {
        return false;
    }

    public void refresh() {
        update();
    }

    @SuppressWarnings("unchecked")
    public void selectionChanged(IFormPart part, ISelection selection) {
        ITreeSelection ts = (ITreeSelection) selection;

        if (!isEmptyArray(ts.getPaths())) {
            this.item = (T) ts.getPaths()[0].getLastSegment();
        } else {
            this.item = null;
        }

        update();
        schemasTable.setInput(item);
    }

    protected abstract void update();
}
