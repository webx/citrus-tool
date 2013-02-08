package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormText;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;

public class SpringPluggableItemDetailsPage extends AbstractTreeItemDetailsPage {
    private FormText namespaceText;
    private Composite schemasComposite;

    @Override
    public void createContents(Composite parent) {
        super.createContents(parent);

        // section
        section.setText("Spring Pluggable Schema");
        section.setDescription("The schema defined in META-INF/spring.schemas");

        // section/client
        Composite client = toolkit.createComposite(section);
        GridLayout glayout = new GridLayout(2, false);
        glayout.marginWidth = 0;
        glayout.marginHeight = 0;
        client.setLayout(glayout);

        section.setClient(client);

        createSpacer(client, 2);

        // Namespace
        toolkit.createLabel(client, "Namespace");
        namespaceText = toolkit.createFormText(client, false);
        namespaceText.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        // Schemas
        //        schemasComposite = toolkit.createComposite(client);
        //        gd = new GridData(GridData.FILL_BOTH);
        //        schemasComposite.setLayoutData(gd);
        //        glayout = new GridLayout(1, false);
        //        glayout.marginWidth = 0;
        //        glayout.marginHeight = 0;
        //        schemasComposite.setLayout(glayout);
    }

    @Override
    protected void update() {
        if (item instanceof SpringPluggableItem) {
            SpringPluggableItem sitem = (SpringPluggableItem) item;

            namespaceText.setText(sitem.getNamespace(), false, true);
        }
    }
}
