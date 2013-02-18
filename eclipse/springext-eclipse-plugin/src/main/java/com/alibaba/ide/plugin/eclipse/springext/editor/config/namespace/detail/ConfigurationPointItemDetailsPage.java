package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.detail;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ConfigurationPointSchemaSourceInfo;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.ide.plugin.eclipse.springext.hyperlink.ConfigurationPointHyperlink;

public class ConfigurationPointItemDetailsPage extends AbstractNamespaceItemDetailsPage<ConfigurationPointItem> {
    private FormText namespaceText;
    private FormText sourceText;

    @Override
    protected void initSection() {
        section.setText("Configuration Point Schema");
        section.setDescription("This namespace and corresponding schema represent a SpringExt Configuration Point.");

        // Namespace
        toolkit.createLabel(client, "Namespace");
        namespaceText = toolkit.createFormText(client, false);
        namespaceText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        // Sources
        toolkit.createLabel(client, "Defined in");
        sourceText = toolkit.createFormText(client, false);
        sourceText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        createSchemasTable();
    }

    @Override
    protected void update() {
        HyperlinkTextBuilder namespaceBuilder = new HyperlinkTextBuilder(toolkit);
        HyperlinkTextBuilder sourcesBuilder = new HyperlinkTextBuilder(toolkit);

        Schema schema = config.getNamespaceDefinitions().getSchemaOfLocation(item.getNamespace(), config.getSchemas());

        // 点击sources，打开configuration point的定义文件。
        ConfigurationPointSchemaSourceInfo sourceInfo = (ConfigurationPointSchemaSourceInfo) schema;
        ConfigurationPointHyperlink link = new ConfigurationPointHyperlink(null, config.getProject(), schema);
        URL url = getSourceURL(sourceInfo.getParent());

        sourcesBuilder.append("<p>").appendLink(url.toExternalForm(), link).append("</p>");

        sourcesBuilder.setText(sourceText);

        // 点击namespace，打开当前所选的schema。
        namespaceBuilder.append("<p>").appendLink(item.getNamespace(), link).append("</p>");

        namespaceBuilder.setText(namespaceText);

        super.update();
    }
}
