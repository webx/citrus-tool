package com.alibaba.eclipse.plugin.webx.extension.hyperlink;

import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class HyperlinkUtil {
    /**
     * 取得当前偏移量所在的结构结点，例如一个element。
     * <p/>
     * Copied from
     * {@link org.eclipse.wst.xml.ui.internal.hyperlink.XMLHyperlinkDetector#getCurrentNode}
     * <p/>
     * Returns the node the cursor is currently on in the document. null if no
     * node is selected
     * 
     * @param offset
     * @return Node either element, doctype, text, or null
     */
    public static Node getCurrentNode(IDocument document, int offset) {
        // get the current node at the offset (returns either: element, doctype, text)
        IndexedRegion inode = null;
        IStructuredModel sModel = null;

        try {
            sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);

            if (sModel != null) {
                inode = sModel.getIndexedRegion(offset);

                if (inode == null) {
                    inode = sModel.getIndexedRegion(offset - 1);
                }
            }
        } finally {
            if (sModel != null) {
                sModel.releaseFromRead();
            }
        }

        if (inode instanceof Node) {
            return (Node) inode;
        }

        return null;
    }

    /**
     * 取得当前偏移量所在的attribute。
     * <p/>
     * Copied from
     * {@link org.eclipse.wst.xml.ui.internal.hyperlink.XMLHyperlinkDetector#getCurrentAttrNode}
     * <p/>
     * Returns the attribute node within node at offset.
     * 
     * @param node
     * @param offset
     * @return Attr
     */
    public static Attr getCurrentAttrNode(Node node, int offset) {
        if ((node instanceof IndexedRegion) && ((IndexedRegion) node).contains(offset) && (node.hasAttributes())) {
            NamedNodeMap attrs = node.getAttributes();

            // go through each attribute in node and if attribute contains
            // offset, return that attribute
            for (int i = 0; i < attrs.getLength(); ++i) {
                // assumption that if parent node is of type IndexedRegion,
                // then its attributes will also be of type IndexedRegion
                IndexedRegion attRegion = (IndexedRegion) attrs.item(i);

                if (attRegion.contains(offset)) {
                    return (Attr) attrs.item(i);
                }
            }
        }

        return null;
    }
}
