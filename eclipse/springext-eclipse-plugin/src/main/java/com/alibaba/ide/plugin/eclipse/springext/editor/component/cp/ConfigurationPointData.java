package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentData;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.PropertiesUtil;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.PropertiesUtil.PropertyModel;

@SuppressWarnings("restriction")
public class ConfigurationPointData extends AbstractSpringExtComponentData<ConfigurationPoint> {
    private final ConfigurationPointViewer documentViewer = new ConfigurationPointViewer();
    private ConfigurationPoint cp;
    private Schema schema;
    private IDocument document;

    public ConfigurationPoint getConfigurationPoint() {
        return cp;
    }

    public Schema getSchema() {
        return schema;
    }

    public ConfigurationPointViewer getDocumentViewer() {
        return documentViewer;
    }

    @Override
    public void initWithEditorInput(IEditorInput input) {
        super.initWithEditorInput(input);
        cp = (ConfigurationPoint) input.getAdapter(ConfigurationPoint.class);
        schema = (Schema) input.getAdapter(Schema.class);
    }

    @Override
    protected void initWithSourceEditor(PropertiesFileEditor sourceEditor) {
        super.initWithSourceEditor(sourceEditor);
        document = sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput());
    }

    @Override
    protected void onSchemaSetChanged() {
        if (cp != null && getSchemas().isSuccessful()) {
            ConfigurationPoint newCp = getSchemas().getConfigurationPoints().getConfigurationPointByName(cp.getName());

            if (newCp != null) {
                cp = newCp;
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
    public void forceRefreshPages() {
        if (managedForm != null) {
            for (IFormPart part : managedForm.getParts()) {
                if (part instanceof AbstractFormPart) {
                    ((AbstractFormPart) part).markStale();
                }
            }
        }
    }

    public class ConfigurationPointViewer implements ModifyListener, ITextListener {
        private final ReentrantLock refreshingLock = new ReentrantLock();
        private Text nameText;
        private Text namespaceText;
        private Text defaultElementText;
        private Text defaultNsPrefixText;
        private ConfigurationPointModel model;

        public void createContent(Composite parent, FormToolkit toolkit) {
            // section/client/name
            toolkit.createLabel(parent, "Name");
            nameText = toolkit.createText(parent, "");
            nameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));

            // section/client/namespace
            toolkit.createLabel(parent, "Namespace");
            namespaceText = toolkit.createText(parent, "");
            namespaceText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));

            // section/client/defaultElementName
            toolkit.createLabel(parent, "Default Element Name");
            defaultElementText = toolkit.createText(parent, "");
            defaultElementText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));

            // section/client/defaultNamespacePrefix
            toolkit.createLabel(parent, "Default Namespace Prefix");
            defaultNsPrefixText = toolkit.createText(parent, "");
            defaultNsPrefixText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, SWT.TOP));

            if (getEditor().isSourceReadOnly()) {
                nameText.setEditable(false);
                namespaceText.setEditable(false);
                defaultElementText.setEditable(false);
                defaultNsPrefixText.setEditable(false);
            } else {
                nameText.addModifyListener(this);
                namespaceText.addModifyListener(this);
                defaultElementText.addModifyListener(this);
                defaultNsPrefixText.addModifyListener(this);
            }
        }

        /**
         * 将字段的修改写入文件中。
         */
        public void modifyText(ModifyEvent e) {
            if (refreshingLock.isLocked()) {
                return;
            }

            ConfigurationPointModel newModel = new ConfigurationPointModel();

            newModel.name = trimToNull(nameText.getText());
            newModel.namespaceUri = trimToNull(namespaceText.getText());
            newModel.defaultElementName = trimToNull(defaultElementText.getText());
            newModel.defaultNsPrefix = trimToNull(defaultNsPrefixText.getText());

            updateDocument(model, newModel);
            model = newModel;
        }

        public void updateDocument(ConfigurationPointModel oldValue, ConfigurationPointModel newValue) {
            PropertiesUtil.updateDocument(document, oldValue.name, newValue.name, newValue.toRawValue());
        }

        /**
         * 当用户直接修改了文件时。
         */
        public void textChanged(TextEvent event) {
            refresh();
        }

        /**
         * 将文件的内容更新到字段中。
         */
        public void refresh() {
            try {
                refreshingLock.lock();
                model = PropertiesUtil.getModel(ConfigurationPointModel.class, document, model == null ? cp.getName()
                        : model.name);

                if (model != null) {
                    nameText.setText(model.name);
                    namespaceText.setText(model.namespaceUri);
                    defaultElementText.setText(defaultIfNull(model.defaultElementName, EMPTY_STRING));
                    defaultNsPrefixText.setText(defaultIfNull(model.defaultNsPrefix, EMPTY_STRING));
                }
            } finally {
                refreshingLock.unlock();
            }
        }
    }

    /**
     * 一个简单的封装类，代表configuration point的编辑数据。
     */
    public static class ConfigurationPointModel extends PropertyModel {
        public String name;
        public String namespaceUri;
        public String defaultElementName;
        public String defaultNsPrefix;

        public ConfigurationPointModel() {
        }

        public ConfigurationPointModel(String key, String rawValue) {
            super(key, rawValue);
            name = key;
            namespaceUri = value;
            defaultElementName = params.remove("defaultElement");
            defaultNsPrefix = params.remove("nsPrefix");
        }

        public String toRawValue() {
            StringBuilder buf = new StringBuilder();

            if (namespaceUri != null) {
                buf.append(namespaceUri);
            }

            if (defaultElementName != null) {
                buf.append(", defaultElement=").append(defaultElementName);
            }

            if (defaultNsPrefix != null) {
                buf.append(", nsPrefix=").append(defaultNsPrefix);
            }

            for (Map.Entry<String, String> entry : params.entrySet()) {
                buf.append(", ").append(entry.getKey()).append("=").append(entry.getValue());
            }

            return buf.toString();
        }

        @Override
        public String toString() {
            return name + " = " + toRawValue();
        }
    }
}
