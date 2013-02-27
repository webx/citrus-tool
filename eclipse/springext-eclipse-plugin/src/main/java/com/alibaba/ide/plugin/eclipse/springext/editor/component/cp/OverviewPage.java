package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import static com.alibaba.citrus.util.BasicConstant.*;

import java.net.URL;
import java.util.Collection;

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
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtFormEditor;
import com.alibaba.ide.plugin.eclipse.springext.hyperlink.ContributionHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.util.HyperlinkTextBuilder;
import com.alibaba.ide.plugin.eclipse.springext.util.HyperlinkTextBuilder.AbstractHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

public class OverviewPage extends FormPage {
    public final static String PAGE_ID = OverviewPage.class.getName();
    private final ConfigurationPointEditor editor;
    private final ConfigurationPointData data;
    private FormToolkit toolkit;
    private SectionPart definitionPart;
    private SectionPart contributionsPart;
    private SectionPart parentsPart;

    public OverviewPage(ConfigurationPointEditor editor) {
        super(editor, PAGE_ID, "Configuration Point");
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
        layout.numColumns = 2;
        layout.horizontalSpacing = 10;
        form.getBody().setLayout(layout);

        definitionPart = new DefinitionPart(form.getBody(), toolkit);
        contributionsPart = new ContributionsPart(form.getBody(), toolkit);
        contributionsPart.getSection().setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 2, 1));
        parentsPart = new ParentsPart(form.getBody(), toolkit);

        managedForm.addPart(definitionPart);
        managedForm.addPart(contributionsPart);
        managedForm.addPart(parentsPart);
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

            // section/client/definedIn
            toolkit.createLabel(client, "Defined in");
            definedInText = toolkit.createFormText(client, false);
            definedInText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/schema
            toolkit.createLabel(client, "Generated Schema");
            schemaText = toolkit.createFormText(client, false);
            schemaText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
        }

        @Override
        public void refresh() {
            data.getDocumentViewer().refresh();

            ConfigurationPoint cp = data.getComponent();

            URL defURL = SpringExtPluginUtil.getSourceURL(cp);

            new HyperlinkTextBuilder(toolkit).append("<p>")
                    .appendLink(defURL.toExternalForm(), new AbstractHyperlink() {
                        public void open() {
                            editor.setActiveTab(SpringExtFormEditor.SOURCE_TAB_KEY);
                        }
                    }).append("</p>").setText(definedInText);

            Schema schema = data.getSchema();

            new HyperlinkTextBuilder(toolkit).append("<p>").appendLink(schema.getName(), new AbstractHyperlink() {
                public void open() {
                    editor.setActiveTab("schema");
                }
            }).append("</p>").setText(schemaText);

            super.refresh();
        }
    }

    private class ContributionsPart extends SectionPart {
        private FormText contributionsText;

        public ContributionsPart(Composite parent, FormToolkit toolkit) {
            super(parent, toolkit, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
            createContents();
        }

        private void createContents() {
            // section
            Section section = getSection();

            section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
            section.setText("Contributions");

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
            contributionsText = toolkit.createFormText(client, false);
            contributionsText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
        }

        @Override
        public void refresh() {
            HyperlinkTextBuilder buf = new HyperlinkTextBuilder(toolkit);
            Schema schema = data.getSchema();
            String version = schema == null ? null : schema.getVersion();

            for (final Contribution contrib : data.getComponent().getContributions()) {
                final Schema contribSchema = contrib.getSchemas().getVersionedSchema(version);

                buf.append("<li style=\"image\" value=\"plug\">")
                        .appendLink(contrib.getName(), new AbstractHyperlink() {
                            public void open() {
                                ContributionHyperlink link = contribSchema == null ? new ContributionHyperlink(null,
                                        data.getProject(), contrib) : new ContributionHyperlink(data.getProject(),
                                        contribSchema);

                                link.open();
                            }
                        }, "nowrap=\"true\"").append("</li>");
            }

            buf.setText(contributionsText);
            contributionsText.setImage("plug", SpringExtPlugin.getDefault().getImageRegistry().get("plug"));

            super.refresh();
        }
    }

    private class ParentsPart extends SectionPart {
        private FormText parentsText;

        public ParentsPart(Composite parent, FormToolkit toolkit) {
            super(parent, toolkit, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
            createContents();
        }

        private void createContents() {
            // section
            Section section = getSection();

            section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
            section.setText("Parent Contributions");

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
            parentsText = toolkit.createFormText(client, false);
            parentsText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
        }

        @Override
        public void refresh() {
            HyperlinkTextBuilder buf = new HyperlinkTextBuilder(toolkit);
            Collection<Contribution> parents = data.getComponent().getDependingContributions();

            if (parents.isEmpty()) {
                parentsText.setText("<no parent>", false, false);
            } else {
                for (final Contribution contrib : parents) {
                    buf.append("<li style=\"image\" value=\"plug\">")
                            .appendLink(contrib.getName(), new AbstractHyperlink() {
                                public void open() {
                                    new ContributionHyperlink(null, data.getProject(), contrib).open();
                                }
                            }, "nowrap=\"true\"").append("</li>");
                }

                buf.setText(parentsText);

                parentsText.setImage("plug", SpringExtPlugin.getDefault().getImageRegistry().get("plug"));
            }

            super.refresh();
        }
    }
}
