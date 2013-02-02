package com.alibaba.ide.plugin.eclipse.springext.extension.editor;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.NamespacesPage;

public class SpringExtConfigEditor extends FormEditor implements IGotoMarker {
    public final static String EDITOR_ID = SpringExtConfigEditor.class.getName();

    // editor & form pages
    private StructuredTextEditor sourceEditor;
    private NamespacesPage namespacesPage;

    // editing data
    private final SpringExtConfig config = new SpringExtConfig(this);

    public StructuredTextEditor getSourceEditor() {
        return sourceEditor;
    }

    public NamespacesPage getNamespacesPage() {
        return namespacesPage;
    }

    public SpringExtConfig getConfig() {
        return config;
    }

    @Override
    protected void addPages() {
        createSourcePage();
        createNamespacesPage();
    }

    /**
     * 创建XML源码页。
     */
    private void createSourcePage() {
        try {
            sourceEditor = new StructuredTextEditor();

            int index = addPage(sourceEditor, getEditorInput());
            setPageText(index, "Source");
            setPartName(sourceEditor.getTitle());
            sourceEditor.setEditorPart(this);
        } catch (PartInitException e) {
            logAndDisplay(new Status(IStatus.ERROR, SpringExtPlugin.PLUGIN_ID,
                    "Could not open editor for source file: " + sourceEditor.getTitle(), e));
        }
    }

    /**
     * 创建namespaces选择页。
     */
    private void createNamespacesPage() {
        try {
            namespacesPage = new NamespacesPage(this);
            int index = addPage(namespacesPage);
            setPageText(index, "Namespaces");
        } catch (PartInitException e) {
            logAndDisplay(new Status(IStatus.ERROR, SpringExtPlugin.PLUGIN_ID, "Could not add tab to editor "
                    + sourceEditor.getTitle(), e));
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        sourceEditor.doSave(monitor);
    }

    @Override
    public void doSaveAs() {
        sourceEditor.doSaveAs();
        setPartName(sourceEditor.getTitle());
        setInput(sourceEditor.getEditorInput());
    }

    @Override
    public boolean isSaveAsAllowed() {
        return sourceEditor != null && sourceEditor.isSaveAsAllowed();
    }

    @Override
    public boolean isSaveOnCloseNeeded() {
        if (sourceEditor != null) {
            return sourceEditor.isSaveOnCloseNeeded();
        } else {
            return super.isSaveOnCloseNeeded();
        }
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);

        if (input instanceof IFileEditorInput) {
            try {
                config.setInput(((IFileEditorInput) input).getFile());
            } catch (Exception e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtPlugin.PLUGIN_ID,
                        "Could not load model for source file: " + sourceEditor.getTitle(), e));
            }
        }
    }

    @Override
    public void dispose() {
        config.dispose();
        super.dispose();
    }

    /**
     * 当用户激发goto marker操作时，跳转到源码页面。
     * 
     * @see IGotoMarker
     */
    public void gotoMarker(IMarker marker) {
        setActiveEditor(sourceEditor);
        IDE.gotoMarker(sourceEditor, marker);
    }
}
