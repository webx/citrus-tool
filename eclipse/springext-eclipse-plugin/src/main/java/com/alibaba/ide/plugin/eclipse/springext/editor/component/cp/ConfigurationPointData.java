package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import org.eclipse.ui.IEditorInput;

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
}
