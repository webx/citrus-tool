package com.alibaba.ide.plugin.eclipse.springext.completion;

import static com.alibaba.citrus.util.Assert.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLStructuredContentAssistProcessor;

/**
 * 使content assist可取得上下文。
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
public class ContextualXMLStructuredContentAssistProcessor extends XMLStructuredContentAssistProcessor {
    private final IAdaptable context;

    public ContextualXMLStructuredContentAssistProcessor(ContentAssistant assistant, String partitionTypeID,
                                                         ITextViewer viewer, IAdaptable context) {
        super(assistant, partitionTypeID, viewer);
        this.context = assertNotNull(context, "no context");
    }

    @Override
    protected CompletionProposalInvocationContext createContext(ITextViewer viewer, int offset) {
        return new ContextualCompletionProposalInvocationContext(viewer, offset);
    }

    private class ContextualCompletionProposalInvocationContext extends CompletionProposalInvocationContext implements
            IAdaptable {
        public ContextualCompletionProposalInvocationContext(ITextViewer viewer, int invocationOffset) {
            super(viewer, invocationOffset);
        }

        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapterClass) {
            return context.getAdapter(assertNotNull(adapterClass));
        }
    }
}
