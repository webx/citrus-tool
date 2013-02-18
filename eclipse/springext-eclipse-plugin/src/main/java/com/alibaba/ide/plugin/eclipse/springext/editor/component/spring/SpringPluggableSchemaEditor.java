package com.alibaba.ide.plugin.eclipse.springext.editor.component.spring;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

@SuppressWarnings("restriction")
public class SpringPluggableSchemaEditor extends AbstractSpringExtComponentEditor<SpringPluggableSchemaSourceInfo> {
    public final static String EDITOR_ID = SpringPluggableSchemaEditor.class.getName();

    // editor & form pages
    private PropertiesFileEditor definitionFileEditor;
    private StructuredTextEditor schemaEditor;
    private StructuredTextEditor generatedSchemaEditor;

    // editing data
    private final SpringPluggableSchemaData data = new SpringPluggableSchemaData();

    @Override
    protected void addPages() {
        createDefinitionFileEditor();
        createSchemaEditor();
        createGeneratedSchemaEditor();
    }

    private void createDefinitionFileEditor() {
        URL definitionURL = getSourceURL(data.getSpringPluggableSchemaSourceInfo().getParent());

        if (definitionURL != null) {
            try {
                definitionFileEditor = new PropertiesFileEditor();
                int index = addPage(definitionFileEditor, new URLEditorInput(definitionURL, data.getProject()));
                setPageText(index, definitionFileEditor.getTitle());
            } catch (PartInitException e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
            }
        }
    }

    private void createSchemaEditor() {
        URL originalSourceURL = getSourceURL(data.getSchema());

        if (originalSourceURL != null) {
            try {
                schemaEditor = new StructuredTextEditor();
                int index = addPage(schemaEditor, new URLEditorInput(originalSourceURL, data.getProject()));
                setPageText(index, schemaEditor.getTitle());
            } catch (PartInitException e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
            }
        }
    }

    private void createGeneratedSchemaEditor() {
        Schema schema = data.getSchema();

        if (schema != null) {
            try {
                generatedSchemaEditor = new StructuredTextEditor();
                int index = addPage(generatedSchemaEditor, new SchemaEditorInput(schema, data.getProject()));
                setPageText(index, generatedSchemaEditor.getTitle());
            } catch (PartInitException e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
            }
        }
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
