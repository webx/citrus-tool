package com.alibaba.ide.plugin.eclipse.springext.extension.hyperlink;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtEclipsePlugin.*;

import java.io.InputStream;

import org.eclipse.core.resources.IProject;
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
import com.alibaba.ide.plugin.eclipse.springext.util.ProjectAware;

/**
 * 用来打开内存中的schema文件的超链接。
 * 
 * @author Michael Zhou
 */
public class SchemaHyperlink implements IHyperlink {
    private final static Logger log = LoggerFactory.getLogger(SchemaHyperlink.class);
    private final IRegion region;
    private final Schema schema;
    private final IProject project;

    public SchemaHyperlink(@NotNull IRegion region, @NotNull Schema schema, @NotNull IProject project) {
        this.region = region;
        this.schema = schema;
        this.project = project;
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
            IEditorInput input = new SchemaEditorInput(schema, project);

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

    private static class SchemaEditorInput implements IStorageEditorInput, ProjectAware {
        private final IStorage storage;
        private final IProject project;

        private SchemaEditorInput(Schema schema, IProject project) {
            this.storage = new SchemaStorage(schema);
            this.project = project;
        }

        public IProject getProject() {
            return project;
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

        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            return null;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof SchemaEditorInput) {
                IStorage otherStorage = ((SchemaEditorInput) other).storage;
                return storage.equals(otherStorage);
            }

            return super.equals(other);
        }
    }
}
