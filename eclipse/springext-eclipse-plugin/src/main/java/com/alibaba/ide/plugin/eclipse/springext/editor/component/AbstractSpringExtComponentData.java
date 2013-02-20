package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import org.eclipse.ui.forms.IManagedForm;

import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtEditingData;

public abstract class AbstractSpringExtComponentData<C> extends SpringExtEditingData {
    protected IManagedForm managedForm;

    public void initWithManagedForm(IManagedForm managedForm) {
        this.managedForm = managedForm;
    }
}
