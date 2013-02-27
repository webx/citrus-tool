package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.ObjectUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstractSpringExtComponentData;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.PropertiesUtil;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.PropertiesUtil.PropertyModel;

public class ConfigurationPointData extends AbstractSpringExtComponentData<ConfigurationPoint> {
    @Override
    protected void onSchemaSetChanged() {
        if (component != null && getSchemas().isSuccessful()) {
            ConfigurationPoint newCp = getSchemas().getConfigurationPoints().getConfigurationPointByName(
                    component.getName());

            if (newCp != null) {
                component = newCp;
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
    protected ConfigurationPointViewer createDocumentViewer() {
        return new ConfigurationPointViewer();
    }

    public class ConfigurationPointViewer extends DocumentViewer {
        private Text nameText;
        private Text namespaceText;
        private Text defaultElementText;
        private Text defaultNsPrefixText;
        private ConfigurationPointModel model;

        @Override
        public void createContent(Composite parent, FormToolkit toolkit) {
            // section/client/name
            toolkit.createLabel(parent, "Name");
            nameText = toolkit.createText(parent, "");
            nameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/namespace
            toolkit.createLabel(parent, "Namespace");
            namespaceText = toolkit.createText(parent, "");
            namespaceText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/defaultElementName
            toolkit.createLabel(parent, "Default Element Name");
            defaultElementText = toolkit.createText(parent, "");
            defaultElementText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

            // section/client/defaultNamespacePrefix
            toolkit.createLabel(parent, "Default Namespace Prefix");
            defaultNsPrefixText = toolkit.createText(parent, "");
            defaultNsPrefixText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));

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
        @Override
        protected void doUpdate() {
            ConfigurationPointModel newModel = new ConfigurationPointModel();

            newModel.name = trimToNull(nameText.getText());
            newModel.namespaceUri = trimToNull(namespaceText.getText());
            newModel.defaultElementName = trimToNull(defaultElementText.getText());
            newModel.defaultNsPrefix = trimToNull(defaultNsPrefixText.getText());

            if (model != null) {
                PropertiesUtil.updateDocument(getDocument(), model.name, newModel.name, newModel.toRawValue());
                model = newModel;
            }
        }

        /**
         * 将文件的内容更新到字段中。
         */
        @Override
        protected void doRefresh() {
            String cpName = model == null ? component.getName() : model.name;
            model = PropertiesUtil.getModel(ConfigurationPointModel.class, getDocument(), cpName);

            if (model != null) {
                nameText.setText(model.name);
                namespaceText.setText(model.namespaceUri);
                defaultElementText.setText(defaultIfNull(model.defaultElementName, EMPTY_STRING));
                defaultNsPrefixText.setText(defaultIfNull(model.defaultNsPrefix, EMPTY_STRING));
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
