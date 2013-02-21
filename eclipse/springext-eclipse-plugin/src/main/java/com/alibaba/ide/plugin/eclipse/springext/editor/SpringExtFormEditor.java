package com.alibaba.ide.plugin.eclipse.springext.editor;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.citrus.util.io.StreamUtil;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

/**
 * 编辑器基类，实现以下功能：
 * <ul>
 * <li>将编辑器和一个data联系在一起。</li>
 * <li>添加和保存每一个tab的信息。</li>
 * <li>实现<code>IAdaptable</code>接口，返回相关对象。</li>
 * <li>从<code>URL</code>中创建input。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public abstract class SpringExtFormEditor<D extends SpringExtEditingData, S extends IEditorPart> extends FormEditor {
    public final static String SOURCE_TAB_KEY = "source";
    private final Map<String, TabInfo> tabs = createHashMap();
    private final D data;
    private final Class<S> sourceEditorType;
    private S sourceEditor;

    public SpringExtFormEditor(D data) {
        this.data = data;
        data.setEditor(this);

        @SuppressWarnings("unchecked")
        Class<S> c = (Class<S>) resolveParameter(getClass(), SpringExtFormEditor.class, 1).getRawType();
        this.sourceEditorType = c;
    }

    public final D getData() {
        return data;
    }

    public final S getSourceEditor() {
        return sourceEditor;
    }

    public final boolean isSourceReadOnly() {
        return getTab(SOURCE_TAB_KEY).readOnly;
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        data.initWithEditorInput(input);
        setPartName(input.getName());
    }

    protected final <T extends IFormPage> T addTab(String tabKey, T page, String tabTitle) {
        try {
            int index = addPage(page);
            setPageText(index, tabTitle);
            getOrCreateTab(tabKey).index = index;
        } catch (PartInitException e) {
            logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
        }

        return page;
    }

    protected final S addSouceTab(S page, IEditorInput input, String tabTitle) {
        return addTab(SOURCE_TAB_KEY, page, input, tabTitle);
    }

    protected final <T extends IEditorPart> T addTab(String tabKey, T page, IEditorInput input, String tabTitle) {
        if (SOURCE_TAB_KEY.equals(tabKey)) {
            this.sourceEditor = sourceEditorType.cast(page);
        }

        try {
            int index = addPage(page, input);
            setPageText(index, tabTitle);

            if (page instanceof StructuredTextEditor) {
                ((StructuredTextEditor) page).setEditorPart(this);
            }

            getOrCreateTab(tabKey).index = index;
        } catch (PartInitException e) {
            logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not add tab to editor", e));
        }

        if (input instanceof IStorageEditorInput) {
            try {
                getTab(tabKey).readOnly = ((IStorageEditorInput) input).getStorage().isReadOnly();
            } catch (CoreException ignored) {
            }
        }

        return page;
    }

    public TabInfo getTab(String key) {
        return assertNotNull(tabs.get(key), "tab key %s does not exist", key);
    }

    public TabInfo getOrCreateTab(String key) {
        TabInfo tab = tabs.get(key);

        if (tab == null) {
            tab = new TabInfo();
            tabs.put(key, tab);
        }

        return tab;
    }

    public final boolean isTabReadOnly(String key) {
        return getTab(key).readOnly;
    }

    public final void setActiveTab(String key) {
        setActivePage(getTab(key).index);
    }

    @Override
    public void dispose() {
        data.dispose();
        tabs.clear();
    }

    protected final IEditorInput createInputFromURL(URL url, String tabKey) {
        assertNotNull(url, "no url");

        IFile file = toFile(url);

        if (file != null) {
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

    private class URLEditorInput extends PlatformObject implements IStorageEditorInput {
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
            if (other instanceof SpringExtFormEditor<?, ?>.URLEditorInput) {
                IStorage otherStorage = ((SpringExtFormEditor<?, ?>.URLEditorInput) other).storage;
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

    public static class TabInfo {
        public int index = -1;
        public boolean readOnly = true;
    }
}
