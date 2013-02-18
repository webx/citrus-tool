package com.alibaba.ide.plugin.eclipse.springext.hyperlink;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.spring.SpringPluggableSchemaEditor;

public class SpringPluggableSchemaHyperlink extends AbstractSpringExtHyperlink<SpringPluggableSchemaSourceInfo> {
    public SpringPluggableSchemaHyperlink(@Nullable IRegion region, @NotNull IProject project,
                                          @NotNull SpringPluggableSchemaSourceInfo springPluggableSchema) {
        super(region, project, springPluggableSchema, (Schema) springPluggableSchema);
    }

    @Override
    protected String getDescription() {
        return getTargetNamespace();
    }

    @Override
    public String getName() {
        String name = getTargetNamespace();
        return name.substring(name.indexOf("/") + 1);
    }

    private String getTargetNamespace() {
        return ((Schema) component).getTargetNamespace();
    }

    @Override
    protected String getEditorId() {
        return SpringPluggableSchemaEditor.EDITOR_ID;
    }

    @Override
    protected Schema getComponentSchema() {
        return null;
    }
}
