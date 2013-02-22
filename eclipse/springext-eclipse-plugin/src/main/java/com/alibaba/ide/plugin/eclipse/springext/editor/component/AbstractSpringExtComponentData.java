package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtEditingData;

@SuppressWarnings("restriction")
public abstract class AbstractSpringExtComponentData<C> extends SpringExtEditingData<PropertiesFileEditor> {
    private DocumentViewer documentViewer;
    protected IManagedForm managedForm;
    protected IDocument document;

    public AbstractSpringExtComponentData() {
        this.documentViewer = createDocumentViewer();
    }

    public void initWithManagedForm(IManagedForm managedForm) {
        this.managedForm = managedForm;
    }

    @Override
    protected void initWithSourceEditor(PropertiesFileEditor sourceEditor) {
        super.initWithSourceEditor(sourceEditor);
        document = sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput());
        document.addDocumentListener(getDocumentViewer());
    }

    public DocumentViewer getDocumentViewer() {
        return documentViewer;
    }

    @Override
    public void forceRefreshPages() {
        if (managedForm != null) {
            for (IFormPart part : managedForm.getParts()) {
                if (part instanceof AbstractFormPart) {
                    ((AbstractFormPart) part).markStale();
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (document != null) {
            document.removeDocumentListener(getDocumentViewer());
        }

        super.dispose();
    }

    protected abstract DocumentViewer createDocumentViewer();

    public abstract class DocumentViewer implements IDocumentListener {
        public abstract void createContent(Composite parent, FormToolkit toolkit);

        public abstract void refresh();
    }
}
