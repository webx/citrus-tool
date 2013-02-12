package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.detail;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;

public class ContributionItemDetailsPage extends AbstractTreeItemDetailsPage<ContributionItem> {
    private FormText sourceText;
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
        sourceText = toolkit.createFormText(client, false);
        sourceText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        toolkit.createLabel(client, "Contribution Type");
        contributionType = toolkit.createFormText(client, false);
        contributionType.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        toolkit.createLabel(client, "Contribution Name");
        contributionName = toolkit.createFormText(client, false);
        contributionName.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        toolkit.createLabel(client, "Contributing to");
        configurationPointName = toolkit.createFormText(client, false);
        configurationPointName.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        toolkit.createLabel(client, "Schemas");
        schemas = toolkit.createFormText(client, false);
        schemas.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        toolkit.createLabel(client, "Child Config Points");
        childConfigurationPoints = toolkit.createFormText(client, false);
        childConfigurationPoints.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
    }

    @Override
    protected void update() {
        Contribution contrib = item.getContribution();

        // sources
        HyperlinkTextBuilder builder = new HyperlinkTextBuilder(toolkit);
        URL url = getSourceURL(contrib);
        builder.append("<p>").appendLink(url.toExternalForm(), new URLHyperlinkListener(url)).append("</p>")
                .setText(sourceText);

        contributionName.setText(contrib.getName(), false, true);
        contributionType.setText(contrib.getType().toString(), false, true);

        // parent configuration point
        Schema parentCpSchema = config.getNamespaceDefinitions().getSchemaOfLocation(
                contrib.getConfigurationPoint().getNamespaceUri(), config.getSchemas());

        builder = new HyperlinkTextBuilder(toolkit);
        builder.append("<p>")
                .appendLink(parentCpSchema.getTargetNamespace(), new SchemaHyperlinkListener(parentCpSchema))
                .append("</p>").setText(configurationPointName);

        // child configuration points
        builder = new HyperlinkTextBuilder(toolkit);

        for (ConfigurationPointItem child : item.getChildren()) {
            Schema childCpSchema = config.getNamespaceDefinitions().getSchemaOfLocation(
                    child.getConfigurationPoint().getNamespaceUri(), config.getSchemas());

            builder.append("<p>")
                    .appendLink(childCpSchema.getTargetNamespace(), new SchemaHyperlinkListener(childCpSchema))
                    .append("</p>");
        }

        builder.setText(childConfigurationPoints);

        // schemas
        builder = new HyperlinkTextBuilder(toolkit);

        for (Schema schema : contrib.getSchemas().getNamedMappings().values()) {
            url = getSourceURL(schema);
            builder.append("<p>").appendLink(url.toExternalForm(), new URLHyperlinkListener(url)).append("</p>");
        }

        builder.setText(schemas);
    }
}
