package com.alibaba.eclipse.plugin.webx.extension.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.FormEditor;

public class SpringExtConfigurationFileEditor extends FormEditor {
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
}
