package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace;

import static com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.dom.DomDocumentUtil.*;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfig;

public class NamespacesMasterPart extends SectionPart {
    private final SpringExtConfig config;
    private final FormToolkit toolkit;
    private FilteredCheckboxTree tree;
    private CheckboxTreeViewer treeViewer;

    public NamespacesMasterPart(Composite parent, NamespacesPage page) {
        super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);

        this.config = page.getConfig();
        this.toolkit = page.getManagedForm().getToolkit();

        createContents();
    }

    private void createContents() {
        // section
        Section section = getSection();

        section.setText("Select Namespaces");
        section.setDescription("The list contains namepsaces that are available to SpringExt/Spring configuration file.");
        section.marginWidth = 10;
        section.marginHeight = 5;

        // section/textClient
        Composite textClient = toolkit.createComposite(section, SWT.NO_BACKGROUND);
        GridLayout layout = new GridLayout(3, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        textClient.setLayout(layout);

        final ImageHyperlink treeListButton = toolkit
                .createImageHyperlink(textClient, SWT.NO_BACKGROUND | SWT.NO_FOCUS);

        final ImageHyperlink collapseButton = toolkit
                .createImageHyperlink(textClient, SWT.NO_BACKGROUND | SWT.NO_FOCUS);
        collapseButton.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("collapse"));
        collapseButton.setToolTipText("Collapse All");

        final ImageHyperlink expandButton = toolkit.createImageHyperlink(textClient, SWT.NO_BACKGROUND | SWT.NO_FOCUS);
        expandButton.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("expand"));
        expandButton.setToolTipText("Expand All");

        updateTreeListButton(treeListButton, collapseButton, expandButton);

        section.setTextClient(textClient);

        // section/client
        Composite client = toolkit.createComposite(section, SWT.WRAP);
        layout = new GridLayout(1, false);
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout(layout);

        // section/client/tree
        tree = new FilteredCheckboxTree(toolkit, client, SWT.CHECK | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL
                | SWT.BORDER, new SchemaPatternfilter(), true);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 20;
        gd.widthHint = 100;
        tree.setLayoutData(gd);
        toolkit.paintBordersFor(client);

        section.setClient(client);

        // section/client/tree viewer
        treeViewer = tree.getViewer();

        config.initWithNamespacesTreeViewer(treeViewer);

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                getManagedForm().fireSelectionChanged(NamespacesMasterPart.this, event.getSelection());
            }
        });

        treeViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                updateNamespaceDefinitions(config, (TreeItem) event.getElement(), event.getChecked());
                config.refreshNamespacesPage();
            }
        });

        NamespacesTreeProvider provider = new NamespacesTreeProvider(config);

        treeViewer.setContentProvider(provider);
        treeViewer.setLabelProvider(provider);
        treeViewer.setCheckStateProvider(provider);
        treeViewer.setInput(config.getDomDocument());

        treeListButton.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                config.setListNamespacesAsTree(!config.isListNamespacesAsTree());
                updateTreeListButton(treeListButton, collapseButton, expandButton);
            }
        });

        collapseButton.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                treeViewer.collapseAll();
            }
        });

        expandButton.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                treeViewer.expandAll();
            }
        });
    }

    private void updateTreeListButton(ImageHyperlink treeListButton, ImageHyperlink collapseButton,
                                      ImageHyperlink expandButton) {
        if (config.isListNamespacesAsTree()) {
            treeListButton.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("list"));
            treeListButton.setToolTipText("View in Flat Mode");

            collapseButton.setEnabled(true);
            expandButton.setEnabled(true);
        } else {
            treeListButton.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("tree"));
            treeListButton.setToolTipText("View in Hierachical Mode");

            collapseButton.setEnabled(false);
            expandButton.setEnabled(false);
        }

        config.refreshNamespacesPage();
    }

    @Override
    public void refresh() {
        config.refreshNamespacesPage();
        super.refresh();
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
