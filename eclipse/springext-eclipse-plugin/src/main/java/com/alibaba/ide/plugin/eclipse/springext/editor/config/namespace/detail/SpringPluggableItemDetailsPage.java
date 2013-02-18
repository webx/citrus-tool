package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.detail;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import java.net.URL;
import java.util.Set;

import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.hyperlink.SpringPluggableSchemaHyperlink;

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

        // 点击sources，打开spring.schemas的定义文件。
        Set<Schema> schemas = item.getSchemas();
        Set<URL> sources = createHashSet();

        for (Schema schema : schemas) {
            SpringPluggableSchemaSourceInfo sourceInfo = (SpringPluggableSchemaSourceInfo) schema;
            URL url = getSourceURL(sourceInfo.getParent());

            if (!sources.contains(url)) {
                sources.add(url);
                sourcesBuilder
                        .append("<p>")
                        .appendLink(url.toExternalForm(),
                                new SpringPluggableSchemaHyperlink(null, config.getProject(), sourceInfo))
                        .append("</p>");
            }
        }

        sourcesBuilder.setText(sourceText);

        // 点击namespace，打开当前所选择的schema。
        Schema schema = config.getNamespaceDefinitions().getSchemaOfLocation(item.getNamespace(), config.getSchemas());

        namespaceBuilder
                .append("<p>")
                .appendLink(
                        item.getNamespace(),
                        new SpringPluggableSchemaHyperlink(null, config.getProject(),
                                (SpringPluggableSchemaSourceInfo) schema)).append("</p>");

        namespaceBuilder.setText(namespaceText);

        super.update();
    }
}
