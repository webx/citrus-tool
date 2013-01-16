package com.alibaba.eclipse.plugin.webx.extension.completion;

import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;

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
        super.addTagNameProposals(contentAssistRequest, childPosition, context);
    }

}
