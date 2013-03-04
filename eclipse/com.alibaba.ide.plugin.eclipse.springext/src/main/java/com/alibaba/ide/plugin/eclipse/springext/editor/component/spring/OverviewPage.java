package com.alibaba.ide.plugin.eclipse.springext.editor.component.spring;

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

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtFormEditor;
import com.alibaba.ide.plugin.eclipse.springext.util.HyperlinkTextBuilder;
import com.alibaba.ide.plugin.eclipse.springext.util.HyperlinkTextBuilder.AbstractHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

public class OverviewPage extends FormPage {
    public final static String PAGE_ID = OverviewPage.class.getName();
    private final SpringPluggableSchemaEditor editor;
    private final SpringPluggableSchemaData data;
    private FormToolkit toolkit;
    private SectionPart definitionPart;

    public OverviewPage(SpringPluggableSchemaEditor editor) {
        super(editor, PAGE_ID, "Spring Pluggable Schema");
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

        managedForm.addPart(definitionPart);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);

        if (active) {
            data.forceRefreshPages();
        }
    }

    private class DefinitionPart extends SectionPart {
        private FormText defaultPrefixText;
        private FormText definedInText;
        private FormText schemaText;
        private FormText schemaGeneratedText;

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

            // section/client/editable fields
            data.getDocumentViewer().createContent(client, toolkit);

            // separator
            toolkit.createLabel(client, EMPTY_STRING).setLayoutData(
                    new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 2));
            toolkit.createSeparator(client, SWT.HORIZONTAL).setLayoutData(
                    new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 2));

            // section/client/defaultPrefix
            toolkit.createLabel(client, "Default Prefix");
            defaultPrefixText = toolkit.createFormText(client, false);
            defaultPrefixText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

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

            Schema schema = data.getSchema(); // schema可能不存在

            if (schema != null) {
                defaultPrefixText.setText(schema.getPreferredNsPrefix(), false, false);

                URL defURL = SpringExtPluginUtil.getSourceURL(((SpringPluggableSchemaSourceInfo) schema).getParent());

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
}
