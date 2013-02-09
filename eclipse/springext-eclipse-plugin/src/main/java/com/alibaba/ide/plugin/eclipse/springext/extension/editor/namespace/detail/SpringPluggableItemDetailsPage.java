package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.detail;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.net.URL;
import java.util.Set;

import org.eclipse.ui.forms.widgets.FormText;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.extension.hyperlink.HyperlinkTextBuilder;

public class SpringPluggableItemDetailsPage extends AbstractNamespaceItemDetailsPage<SpringPluggableItem> {
    private FormText namespaceText;
    private FormText sourceText;

    @Override
    protected void initSection() {
        section.setText("Spring Pluggable Schema");
        section.setDescription("The schema defined in [CLASSPATH]/META-INF/spring.schemas");

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

        // 点击sources，打开spring.schemas的定义文件。
        Set<Schema> schemas = item.getSchemas();
        Set<URL> sources = createHashSet();

        for (Schema schema : schemas) {
            SpringPluggableSchemaSourceInfo sourceInfo = (SpringPluggableSchemaSourceInfo) schema;
            URL url = getSourceURL(sourceInfo.getParent());

            if (!sources.contains(url)) {
                sources.add(url);
                sourcesBuilder.append("<p>").appendLink(url.toExternalForm(), new URLHyperlinkListener(url))
                        .append("</p>");
            }
        }

        sourcesBuilder.setText(sourceText);

        // 点击namespace，打开当前所选择的schema。
        Schema schema = config.getNamespaceDefinitions().getSchemaOfLocation(item.getNamespace(), config.getSchemas());

        namespaceBuilder.append("<p>").appendLink(item.getNamespace(), new SchemaHyperlinkListener(schema))
                .append("</p>");

        namespaceBuilder.setText(namespaceText);

        super.update();
    }
}
