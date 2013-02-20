package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.jetbrains.annotations.NotNull;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.util.io.StreamUtil;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;
import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtFormEditor;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

@SuppressWarnings("restriction")
public abstract class AbstractSpringExtComponentEditor<C, D extends AbstractSpringExtComponentData<C>> extends
        SpringExtFormEditor<D> {
    public AbstractSpringExtComponentEditor(D data) {
        super(data);
    }

    protected final PropertiesFileEditor createPropertiesEditorPage(String key, URL url, String tabTitle) {
        if (url != null) {
            return addTab(key, new PropertiesFileEditor(), createInputFromURL(url, key), tabTitle);
        }

        return null;
    }

    protected final StructuredTextEditor createSchemaEditorPage(String key, Schema schema, String tabTitle) {
        if (schema != null) {
            return addTab(key, new StructuredTextEditor(), new SchemaEditorInput(schema, getData().getProject()),
                    tabTitle);
        }

        return null;
    }

    protected final StructuredTextEditor createSchemaEditorPage(String key, URL url, String tabTitle) {
        if (url != null) {
            return addTab(key, new StructuredTextEditor(), createInputFromURL(url, key), tabTitle);
        }

        return null;
    }

    protected final IEditorInput createInputFromURL(URL url, String tabKey) {
        assertNotNull(url, "no url");

        IFile file = toFile(url);

        if (file != null) {
            if (tabKey != null) {
                getOrCreateTab(tabKey).readOnly = false;
            }

            return new FileEditorInput(file);
        } else {
            return new URLEditorInput(url);
        }
    }

    private IFile toFile(URL url) {
        IFile file = null;
        File javaFile = null;

        try {
            javaFile = new File(url.toURI());
        } catch (URISyntaxException ignored) {
        } catch (IllegalArgumentException ignored) {
        }

        if (javaFile != null) {
            IPath path = Path.fromOSString(javaFile.getAbsolutePath());
            file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
            file = SpringExtPluginUtil.findSourceFile(file);
        }

        return file;
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

        private URLEditorInput(URL url) {
            this.storage = new URLStorage(url);
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

        @Override
        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            return getData().getAdapter(adapter);
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
