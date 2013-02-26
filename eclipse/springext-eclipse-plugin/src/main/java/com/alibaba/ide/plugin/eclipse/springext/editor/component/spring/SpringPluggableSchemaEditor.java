package com.alibaba.ide.plugin.eclipse.springext.editor.component.spring;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

public class SpringPluggableSchemaEditor extends
        AbstractSpringExtComponentEditor<SpringPluggableSchemaSourceInfo, SpringPluggableSchemaData> {
    public final static String EDITOR_ID = SpringPluggableSchemaEditor.class.getName();
    private StructuredTextEditor schemaEditor;

    public SpringPluggableSchemaEditor() {
        super(new SpringPluggableSchemaData());
    }

    public final void openNewSchemaFile(IResource resource) {
        try {
            schemaEditor.setInput(createInputFromURL(resource.getLocationURI().toURL()));
        } catch (MalformedURLException ignored) {
        }

        setPageText(getTab("originalSchema").index, schemaEditor.getTitle());
    }

    @Override
    protected void addPages() {
        // overview page
        addTab("overview", new OverviewPage(this), "Overview");

        // definition file, editable
        URL definitionURL = getSourceURL(getData().getSpringPluggableSchemaSourceInfo().getParent());
        createPropertiesEditorPage(SOURCE_TAB_KEY, definitionURL, getLastSegment(definitionURL.toExternalForm()));

        Schema schema = getData().getSchema(); // schema可能不存在

        if (schema != null) {
            URL originalSourceURL = getSourceURL(getData().getSchema());
            schemaEditor = createSchemaEditorPage("originalSchema", originalSourceURL,
                    getLastSegment(originalSourceURL.toExternalForm()));

            createSchemaEditorPage("generatedSchema", schema, "Generated " + getLastSegment(schema.getName()));
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
