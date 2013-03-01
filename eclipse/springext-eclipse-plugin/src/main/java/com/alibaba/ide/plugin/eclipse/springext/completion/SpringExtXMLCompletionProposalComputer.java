package com.alibaba.ide.plugin.eclipse.springext.completion;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;

import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

@SuppressWarnings("restriction")
public class SpringExtXMLCompletionProposalComputer extends DefaultXMLCompletionProposalComputer {
    @Override
    protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition,
                                            CompletionProposalInvocationContext context) {
        super.addTagInsertionProposals(contentAssistRequest, childPosition, context);
    }

    @Override
    protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition,
                                       CompletionProposalInvocationContext context) {
        SchemaResourceSet schemas = getFromContext(context, SchemaResourceSet.class, false);

        if (schemas == null) {
            return;
        }

        IDOMNode parentNode = (IDOMNode) contentAssistRequest.getParent();
        IDOMNode node = (IDOMNode) contentAssistRequest.getNode();

        contentAssistRequest.addProposal(new SeparatorCompletionProposal(contentAssistRequest, node, parentNode));
    }
}
