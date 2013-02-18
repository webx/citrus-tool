package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

@SuppressWarnings("restriction")
public class ConfigurationPointEditor extends
        AbstractSpringExtComponentEditor<ConfigurationPoint, ConfigurationPointData> {
    public final static String EDITOR_ID = ConfigurationPointEditor.class.getName();

    // editor & form pages
    private ConfigurationPointPage configurationPointPage;
    private PropertiesFileEditor definitionFileEditor;
    private StructuredTextEditor schemaEditor;

    public ConfigurationPointEditor() {
        super(new ConfigurationPointData());
    }

    @Override
    protected void addPages() {
        createConfigurationPointPage();
        createDefinitionFileEditor();
        createSchemaEditor();
    }

    private void createConfigurationPointPage() {
        configurationPointPage = addPage(new ConfigurationPointPage(this), "Configuration Point");
    }

    private void createDefinitionFileEditor() {
        URL definitionURL = getSourceURL(getData().getConfigurationPoint());
        definitionFileEditor = createPropertiesEditorPage(definitionURL, getLastSegment(definitionURL.toExternalForm()));
    }

    private void createSchemaEditor() {
        Schema schema = getData().getSchema();
        schemaEditor = createSchemaEditorPage(schema, "Generated " + getLastSegment(schema.getName()));
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
