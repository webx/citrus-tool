package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail.SpringPluggableItemDetailsPage;

public class NamespacesMasterDetailsBlock extends MasterDetailsBlock implements IDetailsPageProvider {
    private final NamespacesPage page;
    private NamespacesMasterPart masterPart;

    public NamespacesMasterDetailsBlock(NamespacesPage page) {
        this.page = page;
    }

    public NamespacesMasterPart getMasterPart() {
        return masterPart;
    }

    public void setMaster(NamespacesMasterPart master) {
        this.masterPart = master;
    }

    @Override
    protected void createMasterPart(IManagedForm managedForm, Composite parent) {
        masterPart = new NamespacesMasterPart(parent, page);
        managedForm.addPart(masterPart);
        masterPart.createContents();
    }

    @Override
    protected void registerPages(DetailsPart detailsPart) {
        detailsPart.setPageProvider(this);
    }

    public Object getPageKey(Object object) {
        return object;
    }

    public IDetailsPage getPage(Object key) {
        if (key instanceof SpringPluggableItem) {
            if (!((SpringPluggableItem) key).getSchemas().isEmpty()) {
                return new SpringPluggableItemDetailsPage();
            } else {

            }
        } else if (key instanceof ConfigurationPointItem) {

        } else if (key instanceof ContributionItem) {

        }

        return null;
    }

    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
    }
}
