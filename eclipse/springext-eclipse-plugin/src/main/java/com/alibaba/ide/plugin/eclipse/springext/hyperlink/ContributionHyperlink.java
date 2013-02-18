package com.alibaba.ide.plugin.eclipse.springext.hyperlink;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ContributionSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.contrib.ContributionEditor;

public class ContributionHyperlink extends AbstractSpringExtHyperlink<Contribution> {
    public ContributionHyperlink(@Nullable IRegion region, @NotNull IProject project, @NotNull Contribution contrib) {
        super(region, project, contrib, null);
    }

    public ContributionHyperlink(@Nullable IRegion region, @NotNull IProject project, @NotNull Schema schema) {
        super(region, project, (Contribution) ((ContributionSchemaSourceInfo) schema).getParent(), schema);
    }

    @Override
    protected String getDescription() {
        return component.getName() + " - " + component.getConfigurationPoint().getNamespaceUri();
    }

    @Override
    public String getName() {
        return component.getName();
    }

    @Override
    protected String getEditorId() {
        return ContributionEditor.EDITOR_ID;
    }

    @Override
    protected Schema getComponentDefaultSchema() {
        return component.getSchemas().getMainSchema();
    }
}
