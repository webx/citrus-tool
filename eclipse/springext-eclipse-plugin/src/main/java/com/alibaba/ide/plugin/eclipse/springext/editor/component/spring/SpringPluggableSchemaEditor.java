package com.alibaba.ide.plugin.eclipse.springext.editor.component.spring;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

@SuppressWarnings("restriction")
public class SpringPluggableSchemaEditor extends
        AbstractSpringExtComponentEditor<SpringPluggableSchemaSourceInfo, SpringPluggableSchemaData> {
    public final static String EDITOR_ID = SpringPluggableSchemaEditor.class.getName();

    // editor & form pages
    private PropertiesFileEditor definitionFileEditor;
    private StructuredTextEditor schemaEditor;
    private StructuredTextEditor generatedSchemaEditor;

    public SpringPluggableSchemaEditor() {
        super(new SpringPluggableSchemaData());
    }

    @Override
    protected void addPages() {
        createDefinitionFileEditor();
        createSchemaEditor();
        createGeneratedSchemaEditor();
    }

    private void createDefinitionFileEditor() {
        URL definitionURL = getSourceURL(getData().getSpringPluggableSchemaSourceInfo().getParent());
        definitionFileEditor = createPropertiesEditorPage(definitionURL, getLastSegment(definitionURL.toExternalForm()));
    }

    private void createSchemaEditor() {
        URL originalSourceURL = getSourceURL(getData().getSchema());
        schemaEditor = createSchemaEditorPage(originalSourceURL, getLastSegment(originalSourceURL.toExternalForm()));
    }

    private void createGeneratedSchemaEditor() {
        Schema schema = getData().getSchema();
        generatedSchemaEditor = createSchemaEditorPage(schema, "Generated " + getLastSegment(schema.getName()));
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
