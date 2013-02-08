package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import java.io.IOException;

import org.eclipse.ui.forms.widgets.FormText;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ContributionSchemaSourceInfo;
import com.alibaba.citrus.springext.support.ContributionSourceInfo;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;

public class ContributionItemDetailsPage extends AbstractTreeItemDetailsPage<ContributionItem> {
    private FormText location;
    private FormText contributionType;
    private FormText contributionName;
    private FormText configurationPointName;
    private FormText childConfigurationPoints;
    private FormText schemas;

    @Override
    protected void initSection() {
        section.setText("Contribution to Configuration Point");
        section.setDescription("This is a contribution to a configuration point.");

        toolkit.createLabel(client, "Defined in");
        location = toolkit.createFormText(client, false);

        toolkit.createLabel(client, "Contribution Type");
        contributionType = toolkit.createFormText(client, false);

        toolkit.createLabel(client, "Contribution Name");
        contributionName = toolkit.createFormText(client, false);

        toolkit.createLabel(client, "Contributing to");
        configurationPointName = toolkit.createFormText(client, false);

        toolkit.createLabel(client, "Configuration Points");
        childConfigurationPoints = toolkit.createFormText(client, false);

        toolkit.createLabel(client, "Schemas");
        schemas = toolkit.createFormText(client, false);
    }

    @Override
    protected void update() {
        Contribution contrib = item.getContribution();
        ContributionSourceInfo sourceInfo = (ContributionSourceInfo) contrib;

        Resource resource = (Resource) sourceInfo.getSource();

        try {
            location.setText(resource.getURL().toExternalForm(), false, true);
        } catch (IOException ignored) {
        }

        contributionName.setText(contrib.getName(), false, true);
        contributionType.setText(contrib.getType().toString(), false, true);
        configurationPointName.setText(contrib.getConfigurationPoint().getNamespaceUri(), false, true);

        StringBuilder buf = new StringBuilder();

        for (ConfigurationPointItem child : item.getChildren()) {
            if (buf.length() > 0) {
                buf.append("\n");
            }

            buf.append(child.getConfigurationPoint().getNamespaceUri());
        }

        childConfigurationPoints.setText(buf.toString(), false, true);

        buf = new StringBuilder();

        for (Schema schema : contrib.getSchemas().getNamedMappings().values()) {
            if (buf.length() > 0) {
                buf.append("\n");
            }

            resource = (Resource) ((ContributionSchemaSourceInfo) schema).getSource();

            try {
                buf.append(resource.getURL().toExternalForm());
            } catch (IOException ignored) {
            }
        }

        schemas.setText(buf.toString(), false, true);
    }
}
