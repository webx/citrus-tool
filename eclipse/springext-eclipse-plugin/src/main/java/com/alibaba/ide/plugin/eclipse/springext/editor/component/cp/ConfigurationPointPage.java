package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
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
import com.alibaba.ide.plugin.eclipse.springext.util.HyperlinkTextBuilder;
import com.alibaba.ide.plugin.eclipse.springext.util.HyperlinkTextBuilder.AbstractHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

public class ConfigurationPointPage extends FormPage {
    public final static String PAGE_ID = ConfigurationPointPage.class.getName();
    private final ConfigurationPointEditor editor;
    private final ConfigurationPointData data;
    private FormToolkit toolkit;
    private SectionPart definitionPart;
    private SectionPart infoPart;
    private FormText definedInText;
    private Text nameText;
    private Text namespaceText;
    private Text defaultElementText;
    private Text defaultNsPrefixText;

    public ConfigurationPointPage(ConfigurationPointEditor editor) {
        super(editor, PAGE_ID, "Configuration Point");
        this.editor = editor;
        this.data = editor.getData();
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(getTitle());

        TableWrapLayout layout = new TableWrapLayout();
        layout.numColumns = 1;
        form.getBody().setLayout(layout);

        definitionPart = new DefinitionPart(form.getBody(), toolkit);
        infoPart = new InfoPart(form.getBody(), toolkit);

        managedForm.addPart(definitionPart);
        managedForm.addPart(infoPart);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);

        if (active) {
            definitionPart.markStale();
            infoPart.markStale();
        }
    }

    private class DefinitionPart extends SectionPart {
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

            // seciton/client/definedIn
            toolkit.createLabel(client, "Defined in");
            definedInText = toolkit.createFormText(client, false);
            definedInText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));

            // seciton/client/name
            toolkit.createLabel(client, "Name");
            nameText = toolkit.createText(client, "");
            nameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));

            // seciton/client/namespace
            toolkit.createLabel(client, "Namespace");
            namespaceText = toolkit.createText(client, "");
            namespaceText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));

            // seciton/client/defaultElementName
            toolkit.createLabel(client, "Default Element Name");
            defaultElementText = toolkit.createText(client, "");
            defaultElementText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));

            // seciton/client/defaultNamespacePrefix
            toolkit.createLabel(client, "Default Namespace Prefix");
            defaultNsPrefixText = toolkit.createText(client, "");
            defaultNsPrefixText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));
        }

        @Override
        public void refresh() {
            ConfigurationPoint cp = data.getConfigurationPoint();
            URL defURL = SpringExtPluginUtil.getSourceURL(cp);

            new HyperlinkTextBuilder(toolkit).append("<p>")
                    .appendLink(defURL.toExternalForm(), new AbstractHyperlink() {
                        public void open() {
                            editor.activePage("def");
                        }
                    }).append("</p>").setText(definedInText);

            nameText.setText(cp.getName());
            namespaceText.setText(cp.getNamespaceUri());
            defaultElementText.setText(defaultIfNull(cp.getDefaultElementName(), EMPTY_STRING));
            defaultNsPrefixText.setText(defaultIfNull(cp.getPreferredNsPrefix(), EMPTY_STRING));

            super.refresh();
        }
    }

    private class InfoPart extends SectionPart {
        public InfoPart(Composite parent, FormToolkit toolkit) {
            super(parent, toolkit, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
            createContents();
        }

        private void createContents() {
            // section
            Section section = getSection();

            section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));
            section.setText("Informations");
        }
    }
}
