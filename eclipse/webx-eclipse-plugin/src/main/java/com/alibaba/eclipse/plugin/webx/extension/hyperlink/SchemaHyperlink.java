package com.alibaba.eclipse.plugin.webx.extension.hyperlink;

import static com.alibaba.eclipse.plugin.webx.SpringExtEclipsePlugin.*;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.springext.Schema;

/**
 * 用来打开内存中的schema文件的超链接。
 * 
 * @author Michael Zhou
 */
public class SchemaHyperlink implements IHyperlink {
    private final static Logger log = LoggerFactory.getLogger(SchemaHyperlink.class);
    private final IRegion region;
    private final Schema schema;

    public SchemaHyperlink(@NotNull IRegion region, @NotNull Schema schema) {
        this.region = region;
        this.schema = schema;
    }

    public IRegion getHyperlinkRegion() {
        return region;
    }

    public String getTypeLabel() {
        return null;
    }

    public String getHyperlinkText() {
        return String.format("Open '%s%s'", URL_PREFIX, schema.getName());
    }

    public void open() {
        if (schema != null) {
            IEditorInput input = new SchemaStorageEditorInput(new SchemaStorage(schema));

            try {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IDE.openEditor(page, input, XML_EDITOR_ID, true); // 强制使用eclipse自带xml编辑器，否则resolver插件无法工作。
            } catch (PartInitException e) {
                log.error("Could not open editor for schema: {}", schema.getName(), e);
            }
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
            return new Path(schema.getName());
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

    private static class SchemaStorageEditorInput implements IStorageEditorInput {
        private final IStorage storage;

        private SchemaStorageEditorInput(IStorage storage) {
            this.storage = storage;
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
            String path = storage.getFullPath() != null ? storage.getFullPath().toString() : storage.getName();
            return URL_PREFIX + path;
        }

        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            return null;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof SchemaStorageEditorInput) {
                IStorage otherStorage = ((SchemaStorageEditorInput) other).storage;
                return storage.equals(otherStorage);
            }

            return super.equals(other);
        }
    }
}
