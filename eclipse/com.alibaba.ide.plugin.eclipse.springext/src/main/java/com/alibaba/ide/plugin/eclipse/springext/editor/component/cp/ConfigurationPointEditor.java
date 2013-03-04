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
    }

    @Override
    protected void addPages() {
        // overview page
        addTab("overview", new OverviewPage(this), "Overview");

        // definition file, editable
        URL definitionURL = getSourceURL(getData().getComponent());
        createPropertiesEditorPage(SOURCE_TAB_KEY, definitionURL, getLastSegment(definitionURL.toExternalForm()));

        // schema file, read only
        Schema schema = getData().getSchema();
        createSchemaEditorPage("schema", schema, "<generated> " + getLastSegment(schema.getName()));
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        getData().getSourceEditor().doSave(monitor);
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
}
