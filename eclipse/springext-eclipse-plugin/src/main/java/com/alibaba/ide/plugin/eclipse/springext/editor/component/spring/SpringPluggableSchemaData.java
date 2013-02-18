package com.alibaba.ide.plugin.eclipse.springext.editor.component.spring;

import org.eclipse.ui.IEditorInput;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentData;

public class SpringPluggableSchemaData extends AbstractSpringExtComponentData<SpringPluggableSchemaSourceInfo> {
    private Schema schema;

    public SpringPluggableSchemaSourceInfo getSpringPluggableSchemaSourceInfo() {
        return (SpringPluggableSchemaSourceInfo) schema;
    }

    public Schema getSchema() {
        return schema;
    }

    @Override
    public void initWithEditorInput(IEditorInput input) {
        super.initWithEditorInput(input);

        schema = (Schema) input.getAdapter(Schema.class);
    }
}
