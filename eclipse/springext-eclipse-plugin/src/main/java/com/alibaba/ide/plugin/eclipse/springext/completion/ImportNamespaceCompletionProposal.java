package com.alibaba.ide.plugin.eclipse.springext.completion;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.SpringExtConfigData;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.DomDocumentUtil;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

@SuppressWarnings("restriction")
public class ImportNamespaceCompletionProposal extends AbstractInsertNothingCompletionProposal {
    private final TreeItem item;
    private final Object context;
    private final IDOMNode node;

    public ImportNamespaceCompletionProposal(ContentAssistRequest request, Image image, String displayString,
                                             String desc, TreeItem item, Object context, IDOMNode node) {
        super(request, image, displayString, desc);
        this.item = item;
        this.context = context;
        this.node = node;
    }

    @Override
    public void apply(IDocument document) {
        SpringExtConfigData config = SpringExtPluginUtil.getFromContext(context, SpringExtConfigData.class);

        if (config != null) {
            int offset = node.getStartOffset();
            DomDocumentUtil.updateNamespaceDefinitions(config, item, true);
            setCursorPosition(node.getStartOffset() - offset); // 调整光标位置
        }

        super.apply(document);
    }
}
