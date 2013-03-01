package com.alibaba.ide.plugin.eclipse.springext.completion;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLRelevanceConstants;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLStructuredContentAssistProcessor;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.DomDocumentUtil;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.NamespaceDefinition;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.NamespaceDefinitions;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

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

    /**
     * 过滤和排序proposals。
     * <ul>
     * <li>对于顶层items，只列出independent items。</li>
     * <li>将springext的proposals放在前面。</li>
     * </ul>
     */
    @Override
    @SuppressWarnings("rawtypes")
    protected List<ICompletionProposal> filterAndSortProposals(List proposalsUntyped, IProgressMonitor monitor,
                                                               CompletionProposalInvocationContext context) {
        @SuppressWarnings("unchecked")
        List<ICompletionProposal> proposals = createArrayList((List<ICompletionProposal>) proposalsUntyped);

        // 取得标志性的proposal，如果不存在，则不作处理直接返回
        SeparatorCompletionProposal sep = removeSeparatorCompletionProposal(proposals);

        if (sep != null) {
            // 对于beans:beans下的configuration point element，只保留independent items。
            IDOMNode parentNode = sep.getParentNode();
            IDOMDocument document = (IDOMDocument) parentNode.getOwnerDocument();

            if (document.getDocumentElement() == parentNode) {
                NamespaceDefinitions nds = DomDocumentUtil.loadNamespaces(document);

                for (Iterator<ICompletionProposal> i = proposals.iterator(); i.hasNext();) {
                    ICompletionProposal proposal = i.next();

                    if (isTagProposal(proposal)) {
                        String nsPrefix = getNamespacePrefix(proposal);

                        if (!isIndependentOrUnknwonItem(nsPrefix, nds)) {
                            i.remove();
                        }
                    }
                }
            }
        }

        return proposals;
    }

    private boolean isIndependentOrUnknwonItem(String nsPrefix, NamespaceDefinitions nds) {
        if (nsPrefix == null) {
            return true;
        }

        SchemaResourceSet schemas = getFromContext(context, SchemaResourceSet.class);
        NamespaceDefinition nd = nds.findNamespaceByPrefix(nsPrefix);

        if (nd == null) {
            return true;
        }

        String ns = nd.getNamespace();

        // 对于不在schemas中的ns，不处理。
        if (!schemas.getNamespaceMappings().containsKey(ns)) {
            return true;
        }

        // 对于在schemas中的ns，并且是independent item，则返回true。
        for (NamespaceItem item : schemas.getIndependentItems()) {
            if (item.getNamespace().equals(ns)) {
                return true;
            }
        }

        return false;
    }

    private final static Pattern nsPattern = Pattern.compile("(([\\w-]+):)?([\\w-]+)");

    private String getNamespacePrefix(ICompletionProposal proposal) {
        CustomCompletionProposal customCompletionProposal = (CustomCompletionProposal) proposal;
        String replacementString = customCompletionProposal.getReplacementString();
        Matcher matcher = nsPattern.matcher(replacementString);

        if (matcher.find()) {
            return trimToEmpty(matcher.group(2));
        }

        return null;
    }

    private boolean isTagProposal(ICompletionProposal proposal) {
        if (proposal instanceof CustomCompletionProposal) {
            switch (((CustomCompletionProposal) proposal).getRelevance()) {
                case XMLRelevanceConstants.R_TAG_NAME:
                case XMLRelevanceConstants.R_TAG_INSERTION:
                case XMLRelevanceConstants.R_STRICTLY_VALID_TAG_NAME:
                case XMLRelevanceConstants.R_STRICTLY_VALID_TAG_INSERTION:
                    return true;
            }
        }

        return false;
    }

    private SeparatorCompletionProposal removeSeparatorCompletionProposal(List<ICompletionProposal> proposals) {
        for (Iterator<ICompletionProposal> i = proposals.iterator(); i.hasNext();) {
            ICompletionProposal proposal = i.next();

            if (proposal instanceof SeparatorCompletionProposal) {
                i.remove();
                return (SeparatorCompletionProposal) proposal;
            }
        }

        return null;
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
