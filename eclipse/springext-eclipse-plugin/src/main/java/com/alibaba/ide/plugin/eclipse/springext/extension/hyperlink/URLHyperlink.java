package com.alibaba.ide.plugin.eclipse.springext.extension.hyperlink;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin.*;

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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.util.io.StreamUtil;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;
import com.alibaba.ide.plugin.eclipse.springext.util.IProjectAware;

/**
 * 在编辑器中打开URL。如果URL代表一个workspace file，则打开file。
 * 
 * @author Michael Zhou
 */
public class URLHyperlink implements IHyperlink {
    private final static Logger log = LoggerFactory.getLogger(URLHyperlink.class);
    private final IRegion region;
    protected final URL url;
    protected final IFile file;
    protected final IProject project;

    public URLHyperlink(@NotNull IRegion region, @NotNull URL url, @NotNull IProject project) {
        this.region = region;
        this.url = url;
        this.file = toFile(url);
        this.project = project;
    }

    private static IFile toFile(URL url) {
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

    public IRegion getHyperlinkRegion() {
        return region;
    }

    public String getTypeLabel() {
        return null;
    }

    public String getHyperlinkText() {
        return String.format("Open '%s'", url.toString());
    }

    public void open() {
        if (url != null) {
            IEditorInput input;

            if (file != null) {
                input = new FileEditorInput(file);
            } else {
                input = new URLEditorInput(url, project);
            }

            String descriptorId = null;

            try {
                if (isXml(input.getName())) {
                    descriptorId = XML_EDITOR_ID;
                } else {
                    IEditorDescriptor descriptor = IDE.getEditorDescriptor(input.getName());

                    if (descriptor != null) {
                        descriptorId = descriptor.getId();
                    }
                }

                if (descriptorId != null) {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    IDE.openEditor(page, input, descriptorId, true);
                }
            } catch (PartInitException e) {
                log.error("Could not open editor for URL: {}", url, e);
            }
        }
    }

    private boolean isXml(String name) {
        IContentType XML = Platform.getContentTypeManager().getContentType("org.eclipse.wst.xml.core.xmlsource");
        IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(name);

        if (contentType != null) {
            return contentType.isKindOf(XML);
        }

        return false;
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
                throw new CoreException(new Status(IStatus.ERROR, SpringExtPlugin.PLUGIN_ID,
                        "Could not read URL: " + url, e));
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

    private static class URLEditorInput implements IStorageEditorInput, IProjectAware {
        private final IStorage storage;
        private final IProject project;

        private URLEditorInput(URL url, IProject project) {
            this.storage = new URLStorage(url);
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
            return storage.getFullPath() != null ? storage.getFullPath().toString() : storage.getName();
        }

        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            return null;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof URLEditorInput) {
                IStorage otherStorage = ((URLEditorInput) other).storage;
                return storage.equals(otherStorage);
            }

            return super.equals(other);
        }
    }
}
