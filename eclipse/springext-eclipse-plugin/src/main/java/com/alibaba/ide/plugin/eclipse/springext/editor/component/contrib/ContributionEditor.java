package com.alibaba.ide.plugin.eclipse.springext.editor.component.contrib;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

public class ContributionEditor extends AbstractSpringExtComponentEditor<Contribution, ContributionData> {
    public final static String EDITOR_ID = ContributionEditor.class.getName();
    private StructuredTextEditor schemaEditor;

    public ContributionEditor() {
        super(new ContributionData());
    }

    @Override
    protected void addPages() {
        // overview page
        addTab("overview", new OverviewPage(this), "Overview");

        // definition file, editable
        URL definitionURL = getSourceURL(getData().getComponent());
        createPropertiesEditorPage(SOURCE_TAB_KEY, definitionURL, getLastSegment(definitionURL.toExternalForm()));

        Schema schema = getData().getSchema(); // schema可能不存在

        if (schema != null) {
            // schema file, editable
            URL originalSourceURL = getSourceURL(schema);
            schemaEditor = createSchemaEditorPage("originalSchema", originalSourceURL,
                    getLastSegment(originalSourceURL.toExternalForm()));

            // generated schema file, read only
            createSchemaEditorPage("generatedSchema", schema, "<generated> " + getLastSegment(schema.getName()));
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        getData().getSourceEditor().doSave(monitor);

        if (schemaEditor != null) {
            schemaEditor.doSave(monitor);
        }
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
}
