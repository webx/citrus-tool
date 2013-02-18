package com.alibaba.ide.plugin.eclipse.springext.editor.component.spring;

import org.eclipse.ui.IEditorInput;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstactSpringExtComponentData;

public class SpringPluggableSchemaData extends AbstactSpringExtComponentData<SpringPluggableSchemaSourceInfo> {
    private Schema schema;

    public Schema getSchema() {
        return schema;
    }

    @Override
    public void initWithEditorInput(IEditorInput input) {
        super.initWithEditorInput(input);

        schema = (Schema) input.getAdapter(Schema.class);
    }
}
