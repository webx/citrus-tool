package com.alibaba.eclipse.plugin.webx.extension.editor;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class NamespacesPage extends FormPage {
    public final static String PAGE_ID = NamespacesPage.class.getName();
    private NamespacesMasterDetailsBlock block;

    public NamespacesPage(SpringExtConfigurationFileEditor editor) {
        super(editor, PAGE_ID, "Namespaces");
        this.block = new NamespacesMasterDetailsBlock(this);
    }

    @Override
    public SpringExtConfigurationFileEditor getEditor() {
        return (SpringExtConfigurationFileEditor) super.getEditor();
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        form.setText(getTitle());

        block.createContent(managedForm);
    }
}
