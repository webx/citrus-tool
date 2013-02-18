package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentEditor;

@SuppressWarnings("restriction")
public class ConfigurationPointEditor extends
        AbstractSpringExtComponentEditor<ConfigurationPoint, ConfigurationPointData> {
    public final static String EDITOR_ID = ConfigurationPointEditor.class.getName();

    // editor & form pages
    private ConfigurationPointPage configurationPointPage;
    private PropertiesFileEditor definitionFileEditor;
    private StructuredTextEditor schemaEditor;

    public ConfigurationPointEditor(ConfigurationPointData data) {
        super(new ConfigurationPointData());
    }

    @Override
    protected void addPages() {
        createConfigurationPointPage();
        createDefinitionFileEditor();
        createSchemaEditor();
    }

    private void createConfigurationPointPage() {
        try {
            configurationPointPage = new ConfigurationPointPage(this);
            int index = addPage(configurationPointPage);
            setPageText(index, "Configuration Point");
        } catch (PartInitException e) {
            logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
        }
    }

    private void createDefinitionFileEditor() {
        URL definitionURL = getSourceURL(getData().getConfigurationPoint());

        if (definitionURL != null) {
            try {
                definitionFileEditor = new PropertiesFileEditor();
                int index = addPage(definitionFileEditor, new URLEditorInput(definitionURL, getData().getProject()));
                setPageText(index, definitionFileEditor.getTitle());
            } catch (PartInitException e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
            }
        }
    }

    private void createSchemaEditor() {
        Schema schema = getData().getSchema();

        if (schema != null) {
            try {
                schemaEditor = new StructuredTextEditor();
                int index = addPage(schemaEditor, new SchemaEditorInput(schema, getData().getProject()));
                setPageText(index, schemaEditor.getTitle());
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
        getData().initWithEditorInput(input);
    }
}
