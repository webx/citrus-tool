package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentData;

public class ConfigurationPointData extends AbstractSpringExtComponentData<ConfigurationPoint> {
    private ConfigurationPoint cp;
    private Schema schema;

    public ConfigurationPoint getConfigurationPoint() {
        return cp;
    }

    public Schema getSchema() {
        return schema;
    }

    @Override
    public void initWithEditorInput(IEditorInput input) {
        super.initWithEditorInput(input);
        cp = (ConfigurationPoint) input.getAdapter(ConfigurationPoint.class);
        schema = (Schema) input.getAdapter(Schema.class);
    }

    @Override
    protected void onSchemaSetChanged() {
        if (cp != null && getSchemas().isSuccessful()) {
            ConfigurationPoint newCp = getSchemas().getConfigurationPoints().getConfigurationPointByName(cp.getName());

            if (newCp != null) {
                cp = newCp;
            }

            if (schema != null) {
                Schema newSchema = getSchemas().getNamedMappings().get(schema.getName());

                if (newSchema != null) {
                    schema = newSchema;
                }
            }
        }

        super.onSchemaSetChanged();
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
}
