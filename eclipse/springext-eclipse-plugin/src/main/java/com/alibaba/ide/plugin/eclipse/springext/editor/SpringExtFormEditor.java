package com.alibaba.ide.plugin.eclipse.springext.editor;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;

/**
 * 编辑器基类，实现以下功能：
 * <ul>
 * <li>将编辑器和一个data联系在一起。</li>
 * <li>添加和保存每一个tab的信息。</li>
 * <li>实现<code>IAdaptable</code>接口，返回相关对象。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public abstract class SpringExtFormEditor<D extends SpringExtEditingData, S extends IEditorPart> extends FormEditor {
    private final static String SOURCE_TAB_KEY = "source";
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

    public static class TabInfo {
        public int index = -1;
        public boolean readOnly = true;
    }
}
