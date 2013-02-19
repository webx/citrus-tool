package com.alibaba.ide.plugin.eclipse.springext.editor.config;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;
import com.alibaba.ide.plugin.eclipse.springext.editor.SpringExtEditingData;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.DomDocumentUtil;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.NamespaceDefinitions;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

/**
 * 这是SpringExt config editor的核心对象，其中保存了要编辑的文档内容，以及相关的对象。
 * <p/>
 * 此外，它还负责：
 * <ul>
 * <li>当schemas被改变时，更新namespaces列表。</li>
 * <li>当配置文件内容被改变时，更新从文件内容中读取的已选中的namespaces列表。</li>
 * <li>刷新treeViewer，使之反映文档中的选择。</li>
 * <li>支持树状或列表状两种显示模式。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
public class SpringExtConfig extends SpringExtEditingData implements ITextListener {
    private IFile editingFile;

    private IDOMModel model;
    private IDOMDocument domDocument;

    private NamespaceDefinitions nds;

    private IFormPage formPage;
    private StructuredTextViewer textViewer;
    private CheckboxTreeViewer namespacesTreeViewer;
    private boolean listNamespacesAsTree = true;

    public IFile getEditingFile() {
        return editingFile;
    }

    public IDOMDocument getDomDocument() {
        return domDocument;
    }

    @Override
    protected void onSchemaSetChanged() {
        if (namespacesTreeViewer != null) {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    namespacesTreeViewer.setInput(domDocument);
                }
            });
        }
    }

    public NamespaceDefinitions getNamespaceDefinitions() {
        if (nds == null && domDocument != null) {
            nds = DomDocumentUtil.loadNamespaces(domDocument);
        }

        return nds;
    }

    public IManagedForm getManagedForm() {
        return formPage == null ? null : formPage.getManagedForm();
    }

    public void initWithFormPage(IFormPage formPage) {
        this.formPage = formPage;
    }

    public static <T> T getFormPart(Class<T> partType, IManagedForm form) {
        if (form != null) {
            for (IFormPart part : form.getParts()) {
                if (partType.isInstance(part)) {
                    return partType.cast(part);
                }
            }
        }

        return null;
    }

    public <T> T getFormPart(Class<T> partType) {
        return getFormPart(partType, getManagedForm());
    }

    public StructuredTextViewer getTextViewer() {
        return assertNotNull(textViewer, "textViewer is not ready");
    }

    public void initWithTextViewer(StructuredTextViewer viewer) {
        this.textViewer = viewer;
        viewer.addTextListener(this);
    }

    public CheckboxTreeViewer getNamespacesTreeViewer() {
        return assertNotNull(namespacesTreeViewer, "namespacesTreeViewer is not ready");
    }

    public void initWithNamespacesTreeViewer(CheckboxTreeViewer viewer) {
        this.namespacesTreeViewer = viewer;
    }

    public boolean isListNamespacesAsTree() {
        return listNamespacesAsTree;
    }

    public void setListNamespacesAsTree(boolean treeView) {
        this.listNamespacesAsTree = treeView;
    }

    /**
     * 设置要编辑的文件。
     * <p/>
     * 在编辑器被创建时，或者saveAs后，此方法将被调用。
     */
    @Override
    public void initWithEditorInput(IEditorInput input) {
        super.initWithEditorInput(input);

        try {
            if (input instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput) input).getFile();
                IStructuredModel structModel = StructuredModelManager.getModelManager().getExistingModelForEdit(file);

                if (structModel == null) {
                    structModel = StructuredModelManager.getModelManager().getModelForEdit(file);
                }

                if (structModel != null) {
                    if (structModel instanceof IDOMModel) {
                        releaseModel(); // release previous model

                        this.editingFile = file;
                        this.model = (IDOMModel) structModel;
                        this.domDocument = model.getDocument();

                        setSchemas(SchemaResourceSet.getInstance(getProject()));
                    } else {
                        structModel.releaseFromEdit();
                    }
                }
            }
        } catch (Exception e) {
            logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID,
                    "Could not load model for source file: " + input.getName(), e));
        }
    }

    public void textChanged(TextEvent event) {
        nds = null;
    }

    @Override
    public void forceRefreshPages() {
        if (namespacesTreeViewer != null) {
            namespacesTreeViewer.refresh();
        }

        DetailsPart part = getFormPart(DetailsPart.class);

        if (part != null) {
            part.refresh();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if (textViewer != null) {
            textViewer.removeTextListener(this);
        }

        releaseModel();
    }

    private void releaseModel() {
        if (model != null) {
            model.releaseFromEdit();
            model = null;
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IProject.class)) {
            return getProject();
        }

        if (adapter.isAssignableFrom(SchemaResourceSet.class)) {
            return getSchemas();
        }

        return super.getAdapter(adapter);
    }
}
