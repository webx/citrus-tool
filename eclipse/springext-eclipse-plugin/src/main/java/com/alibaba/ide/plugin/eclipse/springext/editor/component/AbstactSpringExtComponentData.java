package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import org.eclipse.ui.IEditorInput;

import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtEditingData;

public abstract class AbstactSpringExtComponentData<C> extends SpringExtEditingData {
    protected IEditorInput input;

    public void initWithEditorInput(IEditorInput input) {
        this.input = input;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(getClass())) {
            return this;
        }

        if (input != null) {
            return input.getAdapter(adapter);
        }

        return super.getAdapter(adapter);
    }
}
