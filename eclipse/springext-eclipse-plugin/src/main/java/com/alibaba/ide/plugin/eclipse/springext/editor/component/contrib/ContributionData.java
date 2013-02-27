package com.alibaba.ide.plugin.eclipse.springext.editor.component.contrib;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;
import static org.eclipse.jdt.core.search.IJavaSearchConstants.*;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentData;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.PropertiesUtil;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.PropertiesUtil.PropertyModel;

@SuppressWarnings("restriction")
public class ContributionData extends AbstractSpringExtComponentData<Contribution> {
    private Contribution contrib;

    public Contribution getContribution() {
        return contrib;
    }

    @Override
    public void initWithEditorInput(IEditorInput input) {
        super.initWithEditorInput(input);
        contrib = (Contribution) input.getAdapter(Contribution.class);
    }

    @Override
    protected void onSchemaSetChanged() {
        if (contrib != null && getSchemas().isSuccessful()) {
            ConfigurationPoint newCp = getSchemas().getConfigurationPoints().getConfigurationPointByName(
                    contrib.getConfigurationPoint().getName());

            Contribution newContrib = newCp == null ? null : newCp
                    .getContribution(contrib.getName(), contrib.getType());

            if (newContrib != null) {
                contrib = newContrib;
            }

            if (schema != null) {
                Schema newSchema = getSchemas().getNamedMappings().get(schema.getName());

                if (newSchema != null) {
                    schema = newSchema;
                }
            }
        }

        super.onSchemaSetChanged();
    }

    @Override
    protected ContributionViewer createDocumentViewer() {
        return new ContributionViewer();
    }

    public class ContributionViewer extends DocumentViewer {
        private Text nameText;
        private Text classNameText;
        private ContributionModel model;

        @Override
        public void createContent(Composite parent, FormToolkit toolkit) {
            // section/client/name
            toolkit.createLabel(parent, "Name");
            nameText = toolkit.createText(parent, "");
            nameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/className label
            Hyperlink classNameLink = toolkit.createHyperlink(parent, "Class Name", SWT.NO_FOCUS);
            classNameLink.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    String className = trimToNull(classNameText.getText());
                    IJavaProject javaProject = getJavaProject(getProject(), false);

                    if (javaProject != null && className != null) {
                        try {
                            JavaUI.openInEditor(javaProject.findType(className));
                        } catch (Exception ignored) {
                        }
                    }
                }
            });

            classNameLink.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));

            // section/client/combo
            Composite combo = toolkit.createComposite(parent);
            combo.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
            TableWrapLayout layout = new TableWrapLayout();
            layout.numColumns = 2;
            layout.leftMargin = 0;
            layout.rightMargin = 0;
            layout.topMargin = 0;
            layout.bottomMargin = 0;
            combo.setLayout(layout);

            // section/client/combo/className
            classNameText = toolkit.createText(combo, "");
            classNameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE));

            // section/client/combo/browse
            Button browse = toolkit.createButton(combo, "Browse", SWT.PUSH);
            browse.setLayoutData(new TableWrapData(TableWrapData.CENTER, TableWrapData.MIDDLE));

            if (getEditor().isSourceReadOnly()) {
                nameText.setEditable(false);
                classNameText.setEditable(false);
            } else {
                nameText.addModifyListener(this);
                classNameText.addModifyListener(this);

                browse.addSelectionListener(new SelectionListener() {
                    public void widgetSelected(SelectionEvent e) {
                        Shell shell = Display.getDefault().getActiveShell();
                        IJavaProject javaProject = getJavaProject(getProject(), false);
                        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject });

                        FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(shell, false, null,
                                scope, CLASS_AND_INTERFACE);

                        dialog.setTitle("Select Type");
                        dialog.setInitialPattern(toClassName(classNameText.getText()));

                        if (dialog.open() == Window.OK) {
                            Object result = dialog.getFirstResult();

                            if (result instanceof IType) {
                                IType type = (IType) result;
                                classNameText.setText(type.getFullyQualifiedName());
                            }
                        }
                    }

                    private String toClassName(String s) {
                        if (s != null) {
                            s = s.replace('$', '.');
                        }

                        return s;
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {
                    }
                });
            }
        }

        /**
         * 将字段的修改写入文件中。
         */
        @Override
        protected void doUpdate() {
            ContributionModel newModel = new ContributionModel();

            newModel.name = trimToNull(nameText.getText());
            newModel.className = trimToNull(classNameText.getText());

            if (model != null) {
                PropertiesUtil.updateDocument(getDocument(), model.name, newModel.name, newModel.className);
                model = newModel;
            }
        }

        /**
         * 将文件的内容更新到字段中。
         */
        @Override
        protected void doRefresh() {
            String contribName = model == null ? contrib.getName() : model.name;
            model = PropertiesUtil.getModel(ContributionModel.class, getDocument(), contribName);

            if (model != null) {
                nameText.setText(model.name);
                classNameText.setText(defaultIfNull(model.className, EMPTY_STRING));
            }
        }
    }

    /**
     * 一个简单的封装类，代表contribution的编辑数据。
     */
    public static class ContributionModel extends PropertyModel {
        public String name;
        public String className;

        public ContributionModel() {
        }

        public ContributionModel(String key, String rawValue) {
            super(key, rawValue);
            name = key;
            className = rawValue;
        }

        @Override
        public String toString() {
            return name + " = " + className;
        }
    }
}
