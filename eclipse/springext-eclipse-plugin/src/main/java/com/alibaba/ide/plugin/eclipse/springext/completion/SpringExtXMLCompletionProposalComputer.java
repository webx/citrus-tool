package com.alibaba.ide.plugin.eclipse.springext.completion;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.util.LinkedList;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.DomDocumentUtil;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.NamespaceDefinitions;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

@SuppressWarnings("restriction")
public class SpringExtXMLCompletionProposalComputer extends DefaultXMLCompletionProposalComputer {
    @Override
    protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition,
                                            CompletionProposalInvocationContext context) {
        addTag(contentAssistRequest, context);
    }

    @Override
    protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition,
                                       CompletionProposalInvocationContext context) {
        addTag(contentAssistRequest, context);
    }

    private void addTag(ContentAssistRequest contentAssistRequest, CompletionProposalInvocationContext context) {
        if (getFromContext(context, SchemaResourceSet.class, false) == null) {
            return;
        }

        IDOMNode parentNode = (IDOMNode) contentAssistRequest.getParent();
        IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
        IDOMDocument document = (IDOMDocument) parentNode.getOwnerDocument();

        // 添加分隔行
        contentAssistRequest.addProposal(new SeparatorCompletionProposal(contentAssistRequest, node, parentNode));

        // import namespace
        SchemaResourceSet schemas = getFromContext(context, SchemaResourceSet.class);
        ContributionItem contributionItem = getContributionItem(parentNode, context);
        NamespaceItem[] items;

        if (contributionItem == null) {
            items = schemas.getIndependentItems();
        } else {
            items = contributionItem.getChildren();
        }

        NamespaceDefinitions nds = DomDocumentUtil.loadNamespaces(document);
        Set<String> importedNamespaces = createHashSet(nds.getNamespaces());
        String filter = contentAssistRequest.getMatchString();

        for (NamespaceItem item : items) {
            if (!importedNamespaces.contains(item.getNamespace()) && item instanceof ConfigurationPointItem) {
                Image image = SpringExtPlugin.getDefault().getImageRegistry().get("socket");
                String nsPrefix = getNamespacePrefix(((ConfigurationPointItem) item).getConfigurationPoint()
                        .getPreferredNsPrefix(), item.getNamespace());
                String desc = String.format("<p><b>Configuration Point: </b></p><p>%s</p>", item.getNamespace());

                if (isEmpty(filter) || nsPrefix.contains(filter)) {
                    contentAssistRequest.addProposal(new ImportNamespaceCompletionProposal(contentAssistRequest, image,
                            "import xmlns:" + nsPrefix, desc, item, context, node));
                }
            }
        }
    }

    private ContributionItem getContributionItem(IDOMNode node, CompletionProposalInvocationContext context) {
        SchemaResourceSet schemas = getFromContext(context, SchemaResourceSet.class);

        while (node != null) {
            if (node instanceof IDOMElement) {
                String ns = trimToNull(node.getNamespaceURI());
                String name = node.getLocalName();

                if (ns != null) {
                    LinkedList<TreeItem> queue = createLinkedList(schemas.getIndependentItems());

                    while (!queue.isEmpty()) {
                        TreeItem item = queue.removeFirst();

                        if (item.hasChildren()) {
                            for (TreeItem child : item.getChildren()) {
                                queue.addLast(child);
                            }
                        }

                        if (item instanceof ContributionItem) {
                            Contribution contrib = ((ContributionItem) item).getContribution();

                            if (contrib.getConfigurationPoint().getNamespaceUri().equals(ns)
                                    && contrib.getName().equals(name)) {
                                return (ContributionItem) item;
                            }
                        }
                    }
                }
            }

            node = (IDOMNode) node.getParentNode();
        }

        return null;
    }
}
