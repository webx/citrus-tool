package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

public class ConfigurationPointEditor extends
        AbstractSpringExtComponentEditor<ConfigurationPoint, ConfigurationPointData> {
    public final static String EDITOR_ID = ConfigurationPointEditor.class.getName();

    public ConfigurationPointEditor() {
        super(new ConfigurationPointData());
        getData().initWithEditor(this);
    }

    @Override
    protected void addPages() {
        createOverviewPage();
        createDefinitionFileEditor();
        createSchemaEditor();
    }

    private void createOverviewPage() {
        addPage("overview", new OverviewPage(this), "Overview");
    }

    private void createDefinitionFileEditor() {
        URL definitionURL = getSourceURL(getData().getConfigurationPoint());
        createPropertiesEditorPage("def", definitionURL, getLastSegment(definitionURL.toExternalForm()));
    }

    private void createSchemaEditor() {
        Schema schema = getData().getSchema();
        createSchemaEditorPage("schema", schema, "<generated> " + getLastSegment(schema.getName()));
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
