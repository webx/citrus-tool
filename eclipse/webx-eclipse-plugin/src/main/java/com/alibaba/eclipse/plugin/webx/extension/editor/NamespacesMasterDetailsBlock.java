package com.alibaba.eclipse.plugin.webx.extension.editor;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

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
        Tree tree = toolkit.createTree(client, SWT.CHECK | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
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
        CheckboxTreeViewer treeViewer = new CheckboxTreeViewer(tree);

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                managedForm.fireSelectionChanged(sectionPart, event.getSelection());
            }
        });

        NamespacesProvider provider = new NamespacesProvider(page.getEditor().getSchemas(), treeViewer);

        treeViewer.setContentProvider(provider);
        treeViewer.setLabelProvider(provider);
        treeViewer.setCheckStateProvider(provider);
        treeViewer.setInput(page.getEditor().getDomDocument());
    }

    @Override
    protected void registerPages(DetailsPart detailsPart) {
    }

    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
    }
}
