package com.alibaba.ide.plugin.eclipse.springext.extension.editor;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.dom.DomDocumentUtil;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.dom.NamespaceDefinitions;
import com.alibaba.ide.plugin.eclipse.springext.schema.ISchemaSetChangeListener;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

/**
 * 这是SpringExt config editor的核心对象，其中保存了要编辑的文档内容，以及相关的对象。
 * <p/>
 * 此外，它还负责：
 * <ul>
 * <li>当schemas被改变时，更新namespaces列表。</li>
 * <li>当配置文件内容被改变时，更新从文件内容中读取的已选中的namespaces列表。</li>
 * <li>刷新treeViewer，使之反映文档中的选择。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
public class SpringExtConfig implements ISchemaSetChangeListener, ITextListener {
    private IProject project;
    private IFile editingFile;

    private IDOMModel model;
    private IDOMDocument domDocument;
    private SchemaResourceSet schemas;
    private NamespaceItem[] allNamespaces;

    private NamespaceDefinitions nds;

    private StructuredTextViewer textViewer;
    private CheckboxTreeViewer namespacesTreeViewer;
    private boolean listNamespacesAsTree = true;

    public IProject getProject() {
        return project;
    }

    public IFile getEditingFile() {
        return editingFile;
    }

    public IDOMDocument getDomDocument() {
        return domDocument;
    }

    public SchemaResourceSet getSchemas() {
        return schemas;
    }

    public void setSchemas(SchemaResourceSet schemas) {
        if (schemas != this.schemas) {
            this.schemas = schemas;
            this.allNamespaces = null;
        }
    }

    public NamespaceItem[] getAllNamespaces() {
        if (allNamespaces == null) {
            Map<String, TreeItem> all = createTreeMap();
            LinkedList<TreeItem> queue = createLinkedList();

            for (NamespaceItem i : schemas.getIndependentItems()) {
                queue.addLast(i);
            }

            while (!queue.isEmpty()) {
                TreeItem item = queue.removeFirst();
                String ns = item instanceof NamespaceItem ? ((NamespaceItem) item).getNamespace() : null;

                // 避免加入重复的ns
                if ((ns == null || !all.containsKey(ns)) && item.hasChildren()) {
                    for (TreeItem c : item.getChildren()) {
                        queue.addLast(c);
                    }
                }

                if (ns != null) {
                    all.put(ns, item);
                }
            }

            allNamespaces = all.values().toArray(new NamespaceItem[all.size()]);
        }

        return allNamespaces;
    }

    public NamespaceDefinitions getNamespaceDefinitions() {
        if (nds == null && domDocument != null) {
            nds = DomDocumentUtil.loadNamespaces(domDocument);
        }

        return nds;
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
    public void setInput(IFile file) throws CoreException, IOException {
        IStructuredModel structModel = StructuredModelManager.getModelManager().getExistingModelForEdit(file);

        if (structModel == null) {
            structModel = StructuredModelManager.getModelManager().getModelForEdit(file);
        }

        if (structModel != null) {
            if (structModel instanceof IDOMModel) {
                releaseModel(); // release previous model

                this.editingFile = file;
                this.project = file.getProject();

                this.model = (IDOMModel) structModel;
                this.domDocument = model.getDocument();
                setSchemas(SchemaResourceSet.getInstance(project));

                SchemaResourceSet.addSchemaSetChangeListener(this);
            } else {
                structModel.releaseFromEdit();
            }
        }
    }

    /**
     * 当schemas被更新时，此方法被调用。
     * <p/>
     * 例如，用户修改了<code>*.bean-definition-parsers</code>文件，或者调整了classpath。
     * 
     * @see ISchemaSetChangeListener
     */
    public void onSchemaSetChanged(SchemaSetChangeEvent event) {
        // 仅当发生变化的project和当前所编辑的文件所在的project是同一个时，才作反应。
        if (event.getProject().equals(project)) {
            setSchemas(SchemaResourceSet.getInstance(project));

            if (namespacesTreeViewer != null) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        namespacesTreeViewer.setInput(domDocument);
                    }
                });
            }
        }
    }

    public void textChanged(TextEvent event) {
        nds = null;
    }

    public void refreshNamespacesPage() {
        if (namespacesTreeViewer != null) {
            namespacesTreeViewer.refresh();
        }
    }

    /**
     * 编辑器被关闭时被调用。
     */
    public void dispose() {
        releaseModel();
        SchemaResourceSet.removeSchemaSetChangeListener(this);

        if (textViewer != null) {
            textViewer.removeTextListener(this);
        }
    }

    private void releaseModel() {
        if (model != null) {
            model.releaseFromEdit();
            model = null;
        }
    }
}
