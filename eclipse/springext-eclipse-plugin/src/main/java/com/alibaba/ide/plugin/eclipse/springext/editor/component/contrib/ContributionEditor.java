package com.alibaba.ide.plugin.eclipse.springext.editor.component.contrib;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

@SuppressWarnings("restriction")
public class ContributionEditor extends AbstractSpringExtComponentEditor<Contribution, ContributionData> {
    public final static String EDITOR_ID = ContributionEditor.class.getName();

    // editor & form pages
    private PropertiesFileEditor definitionFileEditor;
    private StructuredTextEditor schemaEditor;
    private StructuredTextEditor generatedSchemaEditor;

    public ContributionEditor() {
        super(new ContributionData());
    }

    @Override
    protected void addPages() {
        // overview page
        addTab("overview", new OverviewPage(this), "Overview");

        URL definitionURL = getSourceURL(getData().getContribution());
        definitionFileEditor = createPropertiesEditorPage(SOURCE_TAB_KEY, definitionURL,
                getLastSegment(definitionURL.toExternalForm()));

        URL originalSourceURL = getSourceURL(getData().getSchema());
        schemaEditor = createSchemaEditorPage("originalSchema", originalSourceURL,
                getLastSegment(originalSourceURL.toExternalForm()));

        Schema schema = getData().getSchema();
        generatedSchemaEditor = createSchemaEditorPage("generatedSchema", schema, "<generated> "
                + getLastSegment(schema.getName()));
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
