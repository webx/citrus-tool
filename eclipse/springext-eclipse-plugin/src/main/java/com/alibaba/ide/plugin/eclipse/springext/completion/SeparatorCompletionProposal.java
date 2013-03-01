package com.alibaba.ide.plugin.eclipse.springext.completion;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

/**
 * 不作任何事的proposal，起到两个作用：
 * <ul>
 * <li>表现为一条分界线，分隔springext所生成的条目和其它条目。</li>
 * <li>将额外数据传递给processor，以便作过滤。</li>
 * </ul>
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
public class SeparatorCompletionProposal extends AbstractInsertNothingCompletionProposal {
    private final IDOMNode node;
    private final IDOMNode parentNode;

    public SeparatorCompletionProposal(ContentAssistRequest request, IDOMNode node, IDOMNode parentNode) {
        super(request, null, "--------------------", null);

        this.node = node;
        this.parentNode = parentNode;
    }

    public IDOMNode getNode() {
        return node;
    }

    public IDOMNode getParentNode() {
        return parentNode;
    }
}
