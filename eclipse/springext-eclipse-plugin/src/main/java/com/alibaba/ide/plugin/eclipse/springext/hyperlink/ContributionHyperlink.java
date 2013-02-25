package com.alibaba.ide.plugin.eclipse.springext.hyperlink;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ContributionSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.contrib.ContributionEditor;

public class ContributionHyperlink extends AbstractSpringExtHyperlink<Contribution> {
    public ContributionHyperlink(IRegion region, IProject project, Contribution contrib) {
        super(region, project, contrib, null);
    }

    public ContributionHyperlink(IProject project, Schema schema) {
        super(null, project, (Contribution) ((ContributionSchemaSourceInfo) schema).getParent(), schema);
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

    @Override
    protected boolean compareComponent(Contribution thisComponent, Contribution otherComponent) {
        return thisComponent.getName().equals(otherComponent.getName())
                && thisComponent.getConfigurationPoint().getName()
                        .equals(otherComponent.getConfigurationPoint().getName());
    }
}
