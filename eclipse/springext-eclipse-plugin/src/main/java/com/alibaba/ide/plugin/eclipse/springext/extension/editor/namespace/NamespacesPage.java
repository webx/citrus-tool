package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfig;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfigEditor;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail.ConfigurationPointItemDetailsPage;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail.SpringNamespaceHandlerDetailsPage;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail.SpringPluggableItemDetailsPage;

public class NamespacesPage extends FormPage implements IDetailsPageProvider {
    public final static String PAGE_ID = NamespacesPage.class.getName();

    private final NamespacesMasterDetailsBlock block = new NamespacesMasterDetailsBlock();
    private final SpringPluggableItemDetailsPage springPluggableItemDetailsPage = new SpringPluggableItemDetailsPage();
    private final SpringNamespaceHandlerDetailsPage springNamespaceHandlerDetailsPage = new SpringNamespaceHandlerDetailsPage();
    private final ConfigurationPointItemDetailsPage configurationPointItemDetailsPage = new ConfigurationPointItemDetailsPage();

    private final SpringExtConfig config;
    private NamespacesMasterPart masterPart;

    public NamespacesPage(SpringExtConfigEditor editor) {
        super(editor, PAGE_ID, "Namespaces");
        this.config = editor.getConfig();
    }

    public SpringExtConfig getConfig() {
        return config;
    }

    public NamespacesMasterPart getMasterPart() {
        return masterPart;
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        form.setText(getTitle());

        block.createContent(managedForm);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);

        if (active) {
            masterPart.markStale();
        }
    }

    public Object getPageKey(Object object) {
        return object;
    }

    public IDetailsPage getPage(Object key) {
        if (key instanceof SpringPluggableItem) {
            if (!((SpringPluggableItem) key).getSchemas().isEmpty()) {
                return springPluggableItemDetailsPage;
            } else {
                return springNamespaceHandlerDetailsPage;
            }
        } else if (key instanceof ConfigurationPointItem) {
            return configurationPointItemDetailsPage;
        } else if (key instanceof ContributionItem) {

        }

        return null;
    }

    private class NamespacesMasterDetailsBlock extends MasterDetailsBlock {
        @Override
        protected void createMasterPart(IManagedForm managedForm, Composite parent) {
            masterPart = new NamespacesMasterPart(parent, NamespacesPage.this);
            managedForm.addPart(masterPart);
        }

        @Override
        protected void registerPages(DetailsPart detailsPart) {
            detailsPart.setPageProvider(NamespacesPage.this);
        }

        @Override
        protected void createToolBarActions(IManagedForm managedForm) {
            final ScrolledForm form = managedForm.getForm();

            // horizontal button
            Action haction = new Action("horizontal", IAction.AS_RADIO_BUTTON) {
                @Override
                public void run() {
                    sashForm.setOrientation(SWT.HORIZONTAL);
                    form.reflow(true);
                }
            };

            haction.setChecked(true);
            haction.setToolTipText("Horizontal orientation");
            haction.setImageDescriptor(SpringExtPlugin.getDefault().getImageRegistry().getDescriptor("horizontal"));

            // vertical button
            Action vaction = new Action("vertical", IAction.AS_RADIO_BUTTON) {
                @Override
                public void run() {
                    sashForm.setOrientation(SWT.VERTICAL);
                    form.reflow(true);
                }
            };

            vaction.setChecked(false);
            vaction.setToolTipText("Vertical orientation");
            vaction.setImageDescriptor(SpringExtPlugin.getDefault().getImageRegistry().getDescriptor("vertical"));

            form.getToolBarManager().add(haction);
            form.getToolBarManager().add(vaction);
        }
    }
}
