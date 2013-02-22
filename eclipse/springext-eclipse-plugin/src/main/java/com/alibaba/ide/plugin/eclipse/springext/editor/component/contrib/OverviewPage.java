package com.alibaba.ide.plugin.eclipse.springext.editor.component.contrib;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.BasicConstant.*;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtFormEditor;
import com.alibaba.ide.plugin.eclipse.springext.hyperlink.ConfigurationPointHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.util.HyperlinkTextBuilder;
import com.alibaba.ide.plugin.eclipse.springext.util.HyperlinkTextBuilder.AbstractHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

public class OverviewPage extends FormPage {
    public final static String PAGE_ID = OverviewPage.class.getName();
    private final ContributionEditor editor;
    private final ContributionData data;
    private FormToolkit toolkit;
    private SectionPart definitionPart;
    private SectionPart childrenPart;

    public OverviewPage(ContributionEditor editor) {
        super(editor, PAGE_ID, "Contribution");
        this.editor = editor;
        this.data = editor.getData();
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();
        data.initWithManagedForm(managedForm);

        ScrolledForm form = managedForm.getForm();
        form.setText(getTitle());

        TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = 1;
        layout.horizontalSpacing = 10;
        form.getBody().setLayout(layout);

        definitionPart = new DefinitionPart(form.getBody(), toolkit);
        childrenPart = new ChildrenPart(form.getBody(), toolkit);

        managedForm.addPart(definitionPart);
        managedForm.addPart(childrenPart);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);

        if (active) {
            data.forceRefreshPages();
        }
    }

    private class DefinitionPart extends SectionPart {
        private FormText definedInText;
        private FormText schemaText;
        private FormText schemaGeneratedText;
        private FormText belongsToText;
        private FormText contributionTypeText;

        public DefinitionPart(Composite parent, FormToolkit toolkit) {
            super(parent, toolkit, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
            createContents();
        }

        private void createContents() {
            // section
            Section section = getSection();

            section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));
            section.setText("Definition");

            // section/client
            Composite client = toolkit.createComposite(section, SWT.WRAP);

            TableWrapLayout layout = new TableWrapLayout();
            layout.numColumns = 2;
            layout.horizontalSpacing = 10;
            layout.verticalSpacing = 10;
            layout.bottomMargin = 20;

            client.setLayout(layout);
            section.setClient(client);

            // section/client/name
            data.getDocumentViewer().createContent(client, toolkit);

            // separator
            toolkit.createLabel(client, EMPTY_STRING).setLayoutData(
                    new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 2));
            toolkit.createSeparator(client, SWT.HORIZONTAL).setLayoutData(
                    new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 2));

            // section/client/contributionType
            toolkit.createLabel(client, "Type");
            contributionTypeText = toolkit.createFormText(client, false);
            contributionTypeText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/belongsTo
            toolkit.createLabel(client, "Belongs to");
            belongsToText = toolkit.createFormText(client, false);
            belongsToText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/definedIn
            toolkit.createLabel(client, "Defined in");
            definedInText = toolkit.createFormText(client, false);
            definedInText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/schema
            toolkit.createLabel(client, "Schema");
            schemaText = toolkit.createFormText(client, false);
            schemaText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/schema generated
            toolkit.createLabel(client, "Generated Schema");
            schemaGeneratedText = toolkit.createFormText(client, false);
            schemaGeneratedText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
        }

        @Override
        public void refresh() {
            data.getDocumentViewer().refresh();

            Contribution contrib = data.getContribution();
            Schema schema = data.getSchema(); // schema可能不存在

            contributionTypeText.setText(contrib.getType().toString(), false, false);

            if (schema != null) {
                ConfigurationPoint cp = contrib.getConfigurationPoint();
                Schema cpSchema = cp.getSchemas().getVersionedSchema(schema.getVersion());

                new HyperlinkTextBuilder(toolkit).append("<p>")
                        .appendLink(cp.getNamespaceUri(), new ConfigurationPointHyperlink(data.getProject(), cpSchema))
                        .append("</p>").setText(belongsToText);

                URL defURL = SpringExtPluginUtil.getSourceURL(contrib);

                new HyperlinkTextBuilder(toolkit).append("<p>")
                        .appendLink(defURL.toExternalForm(), new AbstractHyperlink() {
                            public void open() {
                                editor.setActiveTab(SpringExtFormEditor.SOURCE_TAB_KEY);
                            }
                        }).append("</p>").setText(definedInText);

                URL schemaURL = SpringExtPluginUtil.getSourceURL(schema);

                new HyperlinkTextBuilder(toolkit).append("<p>")
                        .appendLink(schemaURL.toExternalForm(), new AbstractHyperlink() {
                            public void open() {
                                editor.setActiveTab("originalSchema");
                            }
                        }).append("</p>").setText(schemaText);

                new HyperlinkTextBuilder(toolkit).append("<p>").appendLink(schema.getName(), new AbstractHyperlink() {
                    public void open() {
                        editor.setActiveTab("generatedSchema");
                    }
                }).append("</p>").setText(schemaGeneratedText);
            }

            super.refresh();
        }
    }

    private class ChildrenPart extends SectionPart {
        private FormText childrenText;

        public ChildrenPart(Composite parent, FormToolkit toolkit) {
            super(parent, toolkit, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
            createContents();
        }

        private void createContents() {
            // section
            Section section = getSection();

            section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
            section.setText("Child Configuration Points");

            // section/client
            Composite client = toolkit.createComposite(section, SWT.WRAP);

            TableWrapLayout layout = new TableWrapLayout();
            layout.numColumns = 2;
            layout.horizontalSpacing = 10;
            layout.verticalSpacing = 10;
            layout.bottomMargin = 20;

            client.setLayout(layout);
            section.setClient(client);

            // section/client/contributions
            childrenText = toolkit.createFormText(client, false);
            childrenText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
        }

        @Override
        public void refresh() {
            HyperlinkTextBuilder buf = new HyperlinkTextBuilder(toolkit);
            ConfigurationPointItem[] items = data.getSchemas().getChildConfigurationPoint(data.getContribution());

            if (isEmptyArray(items)) {
                childrenText.setText("<no child>", false, false);
            } else {
                for (ConfigurationPointItem item : items) {
                    final ConfigurationPoint cp = item.getConfigurationPoint();

                    buf.append("<li style=\"image\" value=\"socket\">")
                            .appendLink(cp.getNamespaceUri(), new AbstractHyperlink() {
                                public void open() {
                                    new ConfigurationPointHyperlink(null, data.getProject(), cp).open();
                                }
                            }).append("</li>");
                }

                buf.setText(childrenText);
                childrenText.setImage("socket", SpringExtPlugin.getDefault().getImageRegistry().get("socket"));
            }

            super.refresh();
        }
    }
}
