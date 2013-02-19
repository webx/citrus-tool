package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.forms.IManagedForm;

import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtEditingData;

public abstract class AbstractSpringExtComponentData<C> extends SpringExtEditingData {
    protected IManagedForm managedForm;

    public void initWithManagedForm(IManagedForm managedForm) {
        this.managedForm = managedForm;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }

        if (adapter.isAssignableFrom(IProject.class)) {
            return getProject();
        }

        if (getInput() != null) {
            return getInput().getAdapter(adapter);
        }

        return super.getAdapter(adapter);
    }
}
