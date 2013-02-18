package com.alibaba.ide.plugin.eclipse.springext.editor.component.spring;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;

import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

public class SpringPluggableSchemaEditor extends AbstractSpringExtComponentEditor<SpringPluggableSchemaSourceInfo> {
    public final static String EDITOR_ID = SpringPluggableSchemaEditor.class.getName();

    // editing data
    private final SpringPluggableSchemaData data = new SpringPluggableSchemaData();

    @Override
    protected void addPages() {
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        data.initWithEditorInput(input);
    }
}
