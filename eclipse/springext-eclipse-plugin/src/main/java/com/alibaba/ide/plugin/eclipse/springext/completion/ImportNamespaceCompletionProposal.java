package com.alibaba.ide.plugin.eclipse.springext.completion;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

@SuppressWarnings("restriction")
public class ImportNamespaceCompletionProposal extends AbstractInsertNothingCompletionProposal {
    public ImportNamespaceCompletionProposal(ContentAssistRequest request, Image image, String displayString,
                                             String desc, String nsPrefix, String ns) {
        super(request, image, displayString, desc);
    }
}
