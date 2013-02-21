package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.ui.forms.IManagedForm;

import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtEditingData;

@SuppressWarnings("restriction")
public abstract class AbstractSpringExtComponentData<C> extends SpringExtEditingData<PropertiesFileEditor> {
    protected IManagedForm managedForm;

    public void initWithManagedForm(IManagedForm managedForm) {
        this.managedForm = managedForm;
    }
}
