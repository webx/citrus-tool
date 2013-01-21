package com.alibaba.ide.plugin.eclipse.springext.extension.editor;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.NamespacesPage;
import com.alibaba.ide.plugin.eclipse.springext.extension.resolver.SpringExtSchemaResourceSet;

@SuppressWarnings("restriction")
public class SpringExtConfigurationFileEditor extends FormEditor implements ITextListener {
    public final static String EDITOR_ID = SpringExtConfigurationFileEditor.class.getName();

    // editor & form pages
    private StructuredTextEditor sourceEditor;
    private IFormPage namespacesPage;

    // editing data
    private IFileEditorInput fileInput;
    private IFile file;
    private IDOMModel model;
    private IDOMDocument domDocument;
    private SpringExtSchemaResourceSet schemas;

    public IDOMDocument getDomDocument() {
        return domDocument;
    }

    public SpringExtSchemaResourceSet getSchemas() {
        return schemas;
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
            sourceEditor.getTextViewer().addTextListener(this);
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
        releaseModel();
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
            fileInput = (IFileEditorInput) input;
            resolveModel();
        }
    }

    private void resolveModel() {
        file = fileInput.getFile();

        IStructuredModel structModel = StructuredModelManager.getModelManager().getExistingModelForEdit(file);

        if (structModel == null) {
            try {
                structModel = StructuredModelManager.getModelManager().getModelForEdit(file);
            } catch (Exception e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtPlugin.PLUGIN_ID,
                        "Could not load model for source file: " + sourceEditor.getTitle(), e));
            }
        }

        if (structModel != null) {
            if (structModel instanceof IDOMModel) {
                releaseModel();
                model = (IDOMModel) structModel;
                domDocument = model.getDocument();
                schemas = SpringExtSchemaResourceSet.getInstance(file.getProject());
            } else {
                structModel.releaseFromEdit();
            }
        }
    }

    private void releaseModel() {
        if (model != null) {
            model.releaseFromEdit();
            model = null;
        }
    }

    @Override
    public void dispose() {
        sourceEditor.getTextViewer().removeTextListener(this);
        releaseModel();
        super.dispose();
    }

    /** @see ITextListener */
    public void textChanged(TextEvent event) {
    }
}
