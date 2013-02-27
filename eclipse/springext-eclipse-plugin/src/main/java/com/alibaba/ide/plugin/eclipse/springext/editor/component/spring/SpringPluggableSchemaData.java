package com.alibaba.ide.plugin.eclipse.springext.editor.component.spring;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentData;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.PropertiesUtil;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.PropertiesUtil.PropertyModel;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

public class SpringPluggableSchemaData extends AbstractSpringExtComponentData<SpringPluggableSchemaSourceInfo> {
    public SpringPluggableSchemaSourceInfo getSpringPluggableSchemaSourceInfo() {
        return (SpringPluggableSchemaSourceInfo) schema;
    }

    @Override
    protected void onSchemaSetChanged() {
        if (schema != null && getSchemas().isSuccessful()) {
            Schema newSchema = getSchemas().getNamedMappings().get(schema.getName());

            if (newSchema != null) {
                schema = newSchema;
            }
        }

        super.onSchemaSetChanged();
    }

    @Override
    protected SpringPluggableSchemaViewer createDocumentViewer() {
        return new SpringPluggableSchemaViewer();
    }

    public class SpringPluggableSchemaViewer extends DocumentViewer {
        private Text schemaUriText;
        private Text classpathLocationText;
        private SpringSchemasModel model;

        @Override
        public void createContent(Composite parent, FormToolkit toolkit) {
            // section/client/schemaUri
            toolkit.createLabel(parent, "Schema URI");
            schemaUriText = toolkit.createText(parent, "");
            schemaUriText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/classpathLocation label
            toolkit.createLabel(parent, "Classpath Location").setLayoutData(
                    new TableWrapData(TableWrapData.CENTER, TableWrapData.MIDDLE));

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

            // section/client/combo/classpathLocation
            classpathLocationText = toolkit.createText(combo, "");
            classpathLocationText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE));

            // section/client/combo/browse
            Button browse = toolkit.createButton(combo, "Browse", SWT.PUSH);
            browse.setLayoutData(new TableWrapData(TableWrapData.CENTER, TableWrapData.MIDDLE));

            if (getEditor().isSourceReadOnly()) {
                schemaUriText.setEditable(false);
                classpathLocationText.setEditable(false);
            } else {
                schemaUriText.addModifyListener(this);
                classpathLocationText.addModifyListener(this);

                browse.addSelectionListener(new SelectionListener() {
                    public void widgetSelected(SelectionEvent e) {
                        FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(Display
                                .getDefault().getActiveShell(), false, getProject().getWorkspace().getRoot(),
                                IResource.FILE);

                        dialog.setTitle("Select Resource");
                        dialog.setInitialPattern("**/" + classpathLocationText.getText());

                        if (dialog.open() == Window.OK) {
                            Object result = dialog.getFirstResult();

                            if (result instanceof IFile) {
                                IFile resource = (IFile) result;
                                IJavaProject javaProject = SpringExtPluginUtil.getJavaProject(resource.getProject(),
                                        false);

                                if (javaProject != null) {
                                    try {
                                        IPath path = resource.getFullPath();

                                        for (IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
                                            if (root.getPath().isPrefixOf(path)) {
                                                classpathLocationText.setText(path.removeFirstSegments(
                                                        root.getPath().segmentCount()).toString());

                                                ((SpringPluggableSchemaEditor) getEditor()).openNewSchemaFile(resource);
                                            }
                                        }
                                    } catch (JavaModelException ignored) {
                                    }
                                }
                            }
                        }
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
            SpringSchemasModel newModel = new SpringSchemasModel();

            newModel.schemaUri = trimToNull(schemaUriText.getText());
            newModel.classpathLocation = trimToNull(classpathLocationText.getText());

            if (model != null) {
                PropertiesUtil.updateDocument(getDocument(), model.schemaUri, newModel.schemaUri,
                        newModel.classpathLocation);
                model = newModel;
            }
        }

        /**
         * 将文件的内容更新到字段中。
         */
        @Override
        protected void doRefresh() {
            if (model == null) {
                model = PropertiesUtil.getModel(SpringSchemasModel.class, getDocument(), schema.getName(), true);
            } else {
                model = PropertiesUtil.getModel(SpringSchemasModel.class, getDocument(), model.schemaUri);
            }

            if (model != null) {
                schemaUriText.setText(model.schemaUri);
                classpathLocationText.setText(defaultIfNull(model.classpathLocation, EMPTY_STRING));
            }
        }
    }

    /**
     * 一个简单的封装类，代表spring.schemas的一行数据。
     */
    public static class SpringSchemasModel extends PropertyModel {
        public String schemaUri;
        public String classpathLocation;

        public SpringSchemasModel() {
        }

        public SpringSchemasModel(String key, String rawValue) {
            super(key, rawValue);
            schemaUri = key;
            classpathLocation = rawValue;
        }

        @Override
        public String toString() {
            return schemaUri + " = " + classpathLocation;
        }
    }
}
