package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.jetbrains.annotations.NotNull;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.util.io.StreamUtil;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;

@SuppressWarnings("restriction")
public abstract class AbstractSpringExtComponentEditor<C, D extends AbstractSpringExtComponentData<C>> extends
        FormEditor {
    // editing data
    private final D data;

    public AbstractSpringExtComponentEditor(D data) {
        this.data = data;
    }

    public D getData() {
        return data;
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        getData().initWithEditorInput(input);
        setPartName(input.getName());
    }

    protected final <T extends IFormPage> T addPage(T page, String tabTitle) {
        try {
            int index = addPage(page);
            setPageText(index, tabTitle);
        } catch (PartInitException e) {
            logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
        }

        return page;
    }

    protected final PropertiesFileEditor createPropertiesEditorPage(URL url, String tabTitle) {
        PropertiesFileEditor editor = null;

        if (url != null) {
            try {
                editor = new PropertiesFileEditor();
                int index = addPage(editor, new URLEditorInput(url, getData().getProject()));
                setPageText(index, tabTitle);
            } catch (PartInitException e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
            }
        }

        return editor;
    }

    protected final StructuredTextEditor createSchemaEditorPage(Schema schema, String tabTitle) {
        StructuredTextEditor editor = null;

        if (schema != null) {
            try {
                editor = new StructuredTextEditor();
                int index = addPage(editor, new SchemaEditorInput(schema, getData().getProject()));
                setPageText(index, tabTitle);
            } catch (PartInitException e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
            }
        }

        return editor;
    }

    protected final StructuredTextEditor createSchemaEditorPage(URL url, String tabTitle) {
        StructuredTextEditor editor = null;

        if (url != null) {
            try {
                editor = new StructuredTextEditor();
                int index = addPage(editor, new URLEditorInput(url, getData().getProject()));
                setPageText(index, tabTitle);
            } catch (PartInitException e) {
                logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
            }
        }

        return editor;
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

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(IProject.class)) {
                return data.getProject();
            }

            return super.getAdapter(adapter);
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

        private SchemaStorage(@NotNull Schema schema) {
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

    protected class URLEditorInput extends PlatformObject implements IStorageEditorInput {
        private final IStorage storage;

        public URLEditorInput(URL url, IProject project) {
            this.storage = new URLStorage(url);
        }

        public IProject getProject() {
            return data.getProject();
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
            return storage.getFullPath() != null ? storage.getFullPath().toString() : storage.getName();
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(IProject.class)) {
                return data.getProject();
            }

            return super.getAdapter(adapter);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof AbstractSpringExtComponentEditor<?, ?>.URLEditorInput) {
                IStorage otherStorage = ((AbstractSpringExtComponentEditor<?, ?>.URLEditorInput) other).storage;
                return storage.equals(otherStorage);
            }

            return super.equals(other);
        }
    }

    private static class URLStorage implements IStorage {
        private final URL url;

        private URLStorage(URL url) {
            this.url = url;
        }

        public InputStream getContents() throws CoreException {
            try {
                return StreamUtil.readBytes(url.openStream(), true).toInputStream();
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not read URL: "
                        + url, e));
            }
        }

        public IPath getFullPath() {
            return new Path(url.toString());
        }

        public String getName() {
            return new Path(url.getFile()).lastSegment();
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
            if (other instanceof URLStorage) {
                URL otherURL = ((URLStorage) other).url;
                return url.equals(otherURL);
            }

            return super.equals(other);
        }
    }
}
