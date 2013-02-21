package com.alibaba.ide.plugin.eclipse.springext.editor.config;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtFormEditor;
import com.alibaba.ide.plugin.eclipse.springext.editor.StructuredTextViewerConfigurationSpringExtXML;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.NamespacesPage;

public class SpringExtConfigEditor extends SpringExtFormEditor<SpringExtConfigData, StructuredTextEditor> implements
        IGotoMarker {
    public final static String EDITOR_ID = SpringExtConfigEditor.class.getName();
    private NamespacesPage namespacesPage;

    public SpringExtConfigEditor() {
        super(new SpringExtConfigData());
    }

    public NamespacesPage getNamespacesPage() {
        return namespacesPage;
    }

    @Override
    protected void addPages() {
        createSourcePage();
        createNamespacesPage();
    }

    /**
     * 创建XML源码页。
     */
    private void createSourcePage() {
        StructuredTextEditor sourceEditor = new StructuredTextEditor() {
            @Override
            protected void setSourceViewerConfiguration(SourceViewerConfiguration config) {
                if (config instanceof StructuredTextViewerConfigurationSpringExtXML) {
                    ((StructuredTextViewerConfigurationSpringExtXML) config).setContext(getData());
                }

                super.setSourceViewerConfiguration(config);
            }
        };

        addSouceTab(sourceEditor, getEditorInput(), "Source");
        getData().initWithTextViewer(sourceEditor.getTextViewer());
    }

    /**
     * 创建namespaces选择页。
     */
    private void createNamespacesPage() {
        namespacesPage = new NamespacesPage(this);
        addTab("namespace", namespacesPage, "Namespaces");
        getData().initWithFormPage(namespacesPage);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        getSourceEditor().doSave(monitor);
    }

    @Override
    public void doSaveAs() {
        getSourceEditor().doSaveAs();
        setPartName(getSourceEditor().getTitle());
        setInput(getSourceEditor().getEditorInput());
    }

    @Override
    public boolean isSaveAsAllowed() {
        return getSourceEditor() != null && getSourceEditor().isSaveAsAllowed();
    }

    @Override
    public boolean isSaveOnCloseNeeded() {
        if (getSourceEditor() != null) {
            return getSourceEditor().isSaveOnCloseNeeded();
        } else {
            return super.isSaveOnCloseNeeded();
        }
    }

    /**
     * 当用户激发goto marker操作时，跳转到源码页面。
     * 
     * @see IGotoMarker
     */
    public void gotoMarker(IMarker marker) {
        setActiveEditor(getSourceEditor());
        IDE.gotoMarker(getSourceEditor(), marker);
    }
}
