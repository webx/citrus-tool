package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;

public class NamespacesMasterDetailsBlock extends MasterDetailsBlock {
    private final NamespacesPage page;

    public NamespacesMasterDetailsBlock(NamespacesPage page) {
        this.page = page;
    }

    @Override
    protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
        FormToolkit toolkit = managedForm.getToolkit();

        // section
        Section section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);

        section.setText("Select Namespaces");
        section.setDescription("The list contains namepsaces that are available to SpringExt/Spring configuration file");
        section.marginWidth = 10;
        section.marginHeight = 5;

        // section/client
        Composite client = toolkit.createComposite(section, SWT.WRAP);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout(layout);

        // section/client/tree
        FilteredCheckboxTree tree = new FilteredCheckboxTree(toolkit, client, SWT.CHECK | SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.BORDER, new SchemaPatternfilter(), true);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 20;
        gd.widthHint = 100;
        tree.setLayoutData(gd);
        toolkit.paintBordersFor(client);

        section.setClient(client);

        // section part
        final SectionPart sectionPart = new SectionPart(section);
        managedForm.addPart(sectionPart);

        // section/client/tree viewer
        CheckboxTreeViewer treeViewer = tree.getViewer();

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                managedForm.fireSelectionChanged(sectionPart, event.getSelection());
            }
        });

        NamespacesProvider provider = new NamespacesProvider(page.getEditor().getSchemas());

        treeViewer.setContentProvider(provider);
        treeViewer.setLabelProvider(provider);
        treeViewer.setCheckStateProvider(provider);
        treeViewer.setInput(page.getEditor().getDomDocument());
    }

    @Override
    protected void registerPages(DetailsPart detailsPart) {
        detailsPart.registerPage(SpringPluggableItem.class, new SpringPluggableItemDetailsPage());
    }

    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
    }

    private static class SchemaPatternfilter extends PatternFilter {
        public SchemaPatternfilter() {
            setIncludeLeadingWildcard(true);
        }

        @Override
        public boolean isElementSelectable(Object element) {
            return element != null && !(element instanceof ContributionItem);
        }
    }

    private static class FilteredCheckboxTree extends FilteredTree {
        private FilteredCheckboxTree(FormToolkit toolkit, Composite parent, int treeStyle, PatternFilter filter,
                                     boolean useNewLook) {
            super(parent, treeStyle, filter, useNewLook);
            toolkit.adapt(this, false, false);
        }

        @Override
        public CheckboxTreeViewer getViewer() {
            return (CheckboxTreeViewer) super.getViewer();
        }

        @Override
        protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
            return new CheckboxTreeViewer(parent, style);
        }
    }
}
