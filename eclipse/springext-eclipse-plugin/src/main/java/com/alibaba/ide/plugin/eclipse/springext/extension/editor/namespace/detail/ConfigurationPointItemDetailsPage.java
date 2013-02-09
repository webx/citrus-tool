package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import java.net.URL;

import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ConfigurationPointSchemaSourceInfo;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.ide.plugin.eclipse.springext.extension.hyperlink.HyperlinkTextBuilder;
import com.alibaba.ide.plugin.eclipse.springext.extension.hyperlink.SchemaHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.extension.hyperlink.URLHyperlink;

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

        // Sources
        toolkit.createLabel(client, "Defined in");
        sourceText = toolkit.createFormText(client, false);

        createSchemasTable();
    }

    @Override
    protected void update() {
        HyperlinkTextBuilder namespaceBuilder = new HyperlinkTextBuilder(toolkit);
        HyperlinkTextBuilder sourcesBuilder = new HyperlinkTextBuilder(toolkit);

        final Schema schema = config.getNamespaceDefinitions().getSchemaOfLocation(item.getNamespace(),
                config.getSchemas());

        // 点击sources，打开configuration point的定义文件。
        ConfigurationPointSchemaSourceInfo sourceInfo = (ConfigurationPointSchemaSourceInfo) schema;
        final URL url = getSourceURL(sourceInfo.getParent());

        sourcesBuilder.append("<p>").appendLink(url.toExternalForm(), new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                new URLHyperlink(null, url, config.getProject()).open();
            }
        }).append("</p>");

        sourcesBuilder.setText(sourceText);

        // 点击namespace，打开当前所选的schema。
        namespaceBuilder.append("<p>").appendLink(item.getNamespace(), new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                new SchemaHyperlink(null, schema, config.getProject()).open();
            }
        }).append("</p>");

        namespaceBuilder.setText(namespaceText);

        super.update();
    }
}
