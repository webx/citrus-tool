package com.alibaba.ide.plugin.eclipse.springext.editor.component.contrib;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

public class ContributionEditor extends AbstractSpringExtComponentEditor<Contribution> {
    public final static String EDITOR_ID = ContributionEditor.class.getName();

    // editing data
    private final ContributionData data = new ContributionData();

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
