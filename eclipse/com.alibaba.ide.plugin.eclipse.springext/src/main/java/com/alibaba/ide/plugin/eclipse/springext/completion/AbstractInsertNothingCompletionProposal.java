package com.alibaba.ide.plugin.eclipse.springext.completion;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.sse.ui.internal.contentassist.IRelevanceConstants;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

@SuppressWarnings("restriction")
public abstract class AbstractInsertNothingCompletionProposal extends CustomCompletionProposal {
    public AbstractInsertNothingCompletionProposal(ContentAssistRequest request, Image image, String displayString,
                                                   String additionalProposalInfo) {
        super("", request.getReplacementBeginPosition() + request.getReplacementLength(), 0, 0, image, displayString,
                null, additionalProposalInfo, IRelevanceConstants.R_NONE);
    }
}
