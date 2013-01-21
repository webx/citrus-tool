package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfigEditor;

public class NamespacesPage extends FormPage {
    public final static String PAGE_ID = NamespacesPage.class.getName();
    private NamespacesMasterDetailsBlock block;

    public NamespacesPage(SpringExtConfigEditor editor) {
        super(editor, PAGE_ID, "Namespaces");
        this.block = new NamespacesMasterDetailsBlock(this);
    }

    @Override
    public SpringExtConfigEditor getEditor() {
        return (SpringExtConfigEditor) super.getEditor();
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        form.setText(getTitle());

        block.createContent(managedForm);
    }
}
