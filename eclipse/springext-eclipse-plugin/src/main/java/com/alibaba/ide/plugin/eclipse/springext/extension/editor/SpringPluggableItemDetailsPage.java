package com.alibaba.ide.plugin.eclipse.springext.extension.editor;

import static com.alibaba.citrus.util.ArrayUtil.*;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;

public class SpringPluggableItemDetailsPage implements IDetailsPage {
    private IManagedForm form;
    private TreeItem item;

    // controls
    private FormText namespaceText;
    private Composite schemasComposite;

    public void initialize(IManagedForm form) {
        this.form = form;
    }

    public void dispose() {
    }

    public boolean isDirty() {
        return false;
    }

    public void commit(boolean onSave) {
    }

    public boolean setFormInput(Object input) {
        return false;
    }

    public void setFocus() {
    }

    public boolean isStale() {
        return false;
    }

    public void refresh() {
        update();
    }

    public void selectionChanged(IFormPart part, ISelection selection) {
        ITreeSelection ts = (ITreeSelection) selection;

        if (!isEmptyArray(ts.getPaths())) {
            this.item = (TreeItem) ts.getPaths()[0].getLastSegment();
        } else {
            this.item = null;
        }

        update();
    }

    public void createContents(Composite parent) {
        FormToolkit toolkit = form.getToolkit();

        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 5;
        layout.leftMargin = 5;
        layout.rightMargin = 2;
        layout.bottomMargin = 2;
        parent.setLayout(layout);

        // section
        Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
        section.marginWidth = 10;
        section.setText("Spring Pluggable Schema");
        section.setDescription("The schema defined in META-INF/spring.schemas");

        TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
        td.grabHorizontal = true;
        section.setLayoutData(td);

        // section/client
        Composite client = toolkit.createComposite(section);
        GridLayout glayout = new GridLayout();
        glayout.marginWidth = 0;
        glayout.marginHeight = 0;
        glayout.numColumns = 2;
        client.setLayout(glayout);

        section.setClient(client);

        // Namespace
        Label label = toolkit.createLabel(client, "Namespace");
        namespaceText = toolkit.createFormText(client, false);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        namespaceText.setLayoutData(gd);

        // Schemas
        schemasComposite = toolkit.createComposite(client);
        gd = new GridData(GridData.FILL_BOTH);
        schemasComposite.setLayoutData(gd);
        glayout = new GridLayout();
        glayout.numColumns = 1;
        glayout.marginWidth = 0;
        glayout.marginHeight = 0;
        schemasComposite.setLayout(glayout);
    }

    private void update() {
        if (item instanceof SpringPluggableItem) {
            SpringPluggableItem sitem = (SpringPluggableItem) item;
            
            namespaceText.setText(sitem.getNamespace(), false, true);
            
            
        }
    }
}
