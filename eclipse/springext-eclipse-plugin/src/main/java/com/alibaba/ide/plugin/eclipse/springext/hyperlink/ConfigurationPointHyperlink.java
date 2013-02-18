package com.alibaba.ide.plugin.eclipse.springext.hyperlink;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ConfigurationPointSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.cp.ConfigurationPointEditor;

public class ConfigurationPointHyperlink extends AbstractSpringExtHyperlink<ConfigurationPoint> {
    public ConfigurationPointHyperlink(@Nullable IRegion region, @NotNull IProject project,
                                       @NotNull ConfigurationPoint cp) {
        super(region, project, cp, null);
    }

    public ConfigurationPointHyperlink(@Nullable IRegion region, @NotNull IProject project, @NotNull Schema schema) {
        super(region, project, (ConfigurationPoint) ((ConfigurationPointSchemaSourceInfo) schema).getParent(), schema);
    }

    @Override
    protected String getDescription() {
        return component.getNamespaceUri();
    }

    @Override
    public String getName() {
        String name = component.getName();
        return name.substring(name.indexOf("/") + 1);
    }

    @Override
    protected String getEditorId() {
        return ConfigurationPointEditor.EDITOR_ID;
    }

    @Override
    protected Schema getComponentSchema() {
        return component.getSchemas().getMainSchema();
    }
}
