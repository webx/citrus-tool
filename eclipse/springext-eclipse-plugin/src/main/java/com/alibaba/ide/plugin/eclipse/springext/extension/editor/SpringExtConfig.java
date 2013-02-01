package com.alibaba.ide.plugin.eclipse.springext.extension.editor;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;

import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

/**
 * 这是SpringExt config editor的核心对象，其中保存了要编辑的文档内容，以及相关的对象。
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
public class SpringExtConfig {
    private IProject project;
    private IFile editingFile;

    private IDOMModel model;
    private IDOMDocument domDocument;
    private SchemaResourceSet schemas;

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
                this.schemas = SchemaResourceSet.getInstance(project);
            } else {
                structModel.releaseFromEdit();
            }
        }
    }

    private void releaseModel() {
        if (model != null) {
            model.releaseFromEdit();
            model = null;
        }
    }

    /**
     * 当schemas被更新时，此方法被调用。
     * <p/>
     * 例如，用户修改了<code>*.bean-definition-parsers</code>文件，或者调整了classpath。
     */
    public void updateSchemas() {
        schemas = SchemaResourceSet.getInstance(project);
    }

    /**
     * 编辑器被关闭时被调用。
     */
    public void dispose() {
        releaseModel();
    }
}
