package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.ContextualPropertiesFileEditor;
import com.alibaba.ide.plugin.eclipse.springext.editor.ContextualStructuredTextEditor;
import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtFormEditor;

@SuppressWarnings("restriction")
public abstract class AbstractSpringExtComponentEditor<C, D extends AbstractSpringExtComponentData<C>> extends
        SpringExtFormEditor<D, PropertiesFileEditor> {
    public AbstractSpringExtComponentEditor(D data) {
        super(data);
    }

    @Override
    protected final String getEditorTitleName(String baseName) {
        String version = null;
        Schema schema = getData().getSchema();

        if (schema != null) {
            version = trimToNull(schema.getVersion());
        }

        if (baseName != null && version != null) {
            int i = baseName.lastIndexOf(".");

            if (i < 0) {
                i = baseName.length();
            }

            return baseName.substring(0, i) + "-" + version + baseName.substring(i);
        } else {
            return super.getEditorTitleName(baseName);
        }
    }

    @Override
    protected void createPages() {
        // 假如input为一个简单文件，而不是一个component对象，则直接打开properties文件。
        if (getData().getComponent() == null && getEditorInput() != null) {
            IEditorInput input = getEditorInput();

            if (input != null) {
                addTab(SOURCE_TAB_KEY, new ContextualPropertiesFileEditor(getData()), input,
                        getLastSegment(input.getName()));
            }
        } else {
            super.createPages(); // call addPages()
        }
    }

    protected final PropertiesFileEditor createPropertiesEditorPage(String key, URL url, String tabTitle) {
        if (url != null) {
            return addTab(key, new ContextualPropertiesFileEditor(getData()), createInputFromURL(url), tabTitle);
        }

        return null;
    }

    protected final StructuredTextEditor createSchemaEditorPage(String key, Schema schema, String tabTitle) {
        if (schema != null) {
            return addTab(key, new ContextualStructuredTextEditor(getData()), new SchemaEditorInput(schema, getData()
                    .getProject()), tabTitle);
        }

        return null;
    }

    protected final StructuredTextEditor createSchemaEditorPage(String key, URL url, String tabTitle) {
        if (url != null) {
            return addTab(key, new ContextualStructuredTextEditor(getData()), createInputFromURL(url), tabTitle);
        }

        return null;
    }

    protected class SchemaEditorInput extends PlatformObject implements IStorageEditorInput {
        private final IStorage storage;

        public SchemaEditorInput(Schema schema, IProject project) {
            this.storage = new SchemaStorage(schema);
        }

        public IStorage getStorage() throws CoreException {
            return storage;
        }

        public boolean exists() {
            return storage != null;
        }

        public ImageDescriptor getImageDescriptor() {
            return ImageDescriptor.getMissingImageDescriptor();
        }

        public String getName() {
            return storage.getName();
        }

        public IPersistableElement getPersistable() {
            return null;
        }

        public String getToolTipText() {
            return storage.getFullPath().toString();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            return getData().getAdapter(adapter);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof AbstractSpringExtComponentEditor<?, ?>.SchemaEditorInput) {
                IStorage otherStorage = ((AbstractSpringExtComponentEditor<?, ?>.SchemaEditorInput) other).storage;
                return storage.equals(otherStorage);
            }

            return super.equals(other);
        }
    }

    private static class SchemaStorage implements IStorage {
        private final Schema schema;

        private SchemaStorage(Schema schema) {
            this.schema = schema;
        }

        public InputStream getContents() throws CoreException {
            return schema.getInputStream();
        }

        public IPath getFullPath() {
            return new Path(URL_PREFIX).append(schema.getName());
        }

        public String getName() {
            return new Path(schema.getName()).lastSegment();
        }

        public boolean isReadOnly() {
            return true;
        }

        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            return null;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof SchemaStorage) {
                String otherName = ((SchemaStorage) other).schema.getName();
                return schema.getName().equals(otherName);
            }

            return super.equals(other);
        }
    }
}
