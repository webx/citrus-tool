package com.alibaba.ide.plugin.eclipse.springext.editor;

import org.eclipse.ui.forms.editor.FormEditor;

/**
 * 编辑器基类，实现以下功能：
 * <ul>
 * <li>将编辑器和一个data联系在一起。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
public abstract class SpringExtFormEditor<D extends SpringExtEditingData> extends FormEditor {
    private final D data;

    public SpringExtFormEditor(D data) {
        this.data = data;
        data.setEditor(this);
    }

    public final D getData() {
        return data;
    }

    @Override
    public void dispose() {
        data.dispose();
    }
}
