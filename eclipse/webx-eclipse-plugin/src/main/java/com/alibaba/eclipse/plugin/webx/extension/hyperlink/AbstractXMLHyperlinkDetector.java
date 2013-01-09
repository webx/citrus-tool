package com.alibaba.eclipse.plugin.webx.extension.hyperlink;

import static com.alibaba.citrus.util.ArrayUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * 用于检测XML文档中的超链的基类。
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
public abstract class AbstractXMLHyperlinkDetector extends AbstractHyperlinkDetector {
    public final IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
        if (region == null || textViewer == null) {
            return null;
        }

        IHyperlink[] results = null;

        IDocument document = textViewer.getDocument();
        int currentOffset = region.getOffset();
        Node currentNode = HyperlinkUtil.getCurrentNode(document, currentOffset);

        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
            Detector detector = new Detector(document, currentNode, currentOffset);

            results = detector.visitOpenTag();

            if (isEmptyArray(results)) {
                results = detector.visitCloseTag();
            }

            if (isEmptyArray(results)) {
                Attr attr = HyperlinkUtil.getCurrentAttrNode(currentNode, currentOffset);

                if (attr != null) {
                    results = new Detector(document, attr, currentOffset).visitAttr();
                }
            }
        }

        return results;
    }

    protected IHyperlink[] visitTagPrefix(IDocument document, Region region, String namespaceURI) {
        return null;
    }

    protected IHyperlink[] visitTagName(IDocument document, Region region) {
        return null;
    }

    protected IHyperlink[] visitAttrPrefix(IDocument document, Region region, String namespaceURI) {
        return null;
    }

    protected IHyperlink[] visitAttrName(IDocument document, Region region) {
        return null;
    }

    protected IHyperlink[] visitAttrValue(IDocument document, Region region) {
        return null;
    }

    private class Detector {
        private final IDocument document;
        private final Node currentNode;
        private final int currentOffset;
        private final String qName;
        private final String prefix;
        private final String localName;
        private final String namespaceURI;

        public Detector(IDocument document, Node currentNode, int currentOffset) {
            this.document = document;
            this.currentNode = currentNode;
            this.currentOffset = currentOffset;
            this.qName = trimToNull(currentNode.getNodeName());
            this.prefix = trimToNull(currentNode.getPrefix());
            this.localName = trimToNull(currentNode.getLocalName());
            this.namespaceURI = trimToNull(currentNode.getNamespaceURI());
        }

        /** open tag: &lt;prefix:name */
        public IHyperlink[] visitOpenTag() {
            int startOffset = getOpenTagOffset();

            if (startOffset >= 0) {
                return visitQName(startOffset, true);
            }

            return null;
        }

        /** close tag: &lt;/prefix:name */
        public IHyperlink[] visitCloseTag() {
            int startOffset = getCloseTagOffset();

            if (startOffset >= 0) {
                return visitQName(startOffset, true);
            }

            return null;
        }

        /** attr */
        public IHyperlink[] visitAttr() {
            IHyperlink[] results = null;

            if (currentNode instanceof IDOMAttr) {
                IDOMAttr iattr = (IDOMAttr) currentNode;

                if (isCursorWithin(iattr.getNameRegionStartOffset(), iattr.getNameRegionText())) {
                    results = visitQName(iattr.getNameRegionStartOffset(), false);
                }

                if (isEmptyArray(results)) {
                    String value = iattr.getValueRegionText();

                    if (value != null) {
                        int startOffset = iattr.getValueRegionStartOffset();

                        // 除去前后引号
                        if (value.matches("^[\\\"\\'].*")) {
                            value = value.substring(1);
                            startOffset++;
                        }

                        if (value.matches(".*[\\\"\\']$")) {
                            value = value.substring(0, value.length() - 1);
                        }

                        if (isCursorWithin(startOffset, value)) {
                            results = visitAttrValue(document, new Region(startOffset, value.length()));
                        }
                    }
                }
            }

            return results;
        }

        private IHyperlink[] visitQName(int startOffset, boolean isElement) {
            if (prefix != null && isCursorWithin(startOffset, prefix)) {
                if (isElement) {
                    return visitTagPrefix(document, new Region(startOffset, prefix.length()), namespaceURI);
                } else {
                    return visitAttrPrefix(document, new Region(startOffset, prefix.length()), namespaceURI);
                }
            }

            if (localName != null) {
                int adjust = qName.indexOf(localName, qName.indexOf(":") + 1);

                if (adjust >= 0 && isCursorWithin(startOffset + adjust, localName)) {
                    if (isElement) {
                        return visitTagName(document, new Region(startOffset + adjust, localName.length()));
                    } else {
                        return visitAttrName(document, new Region(startOffset + adjust, localName.length()));
                    }
                }
            }

            return null;
        }

        /**
         * 取得prefix:name的起始偏移量。
         */
        private int getOpenTagOffset() {
            if (currentNode instanceof IDOMElement
                    && ((IDOMElement) currentNode).getStartStructuredDocumentRegion() != null) {
                IStructuredDocumentRegion start = ((IDOMElement) currentNode).getStartStructuredDocumentRegion();
                ITextRegionList regions = start.getRegions();

                if (regions != null) {
                    for (Iterator<?> i = regions.iterator(); i.hasNext();) {
                        ITextRegion region = (ITextRegion) i.next();

                        if (DOMRegionContext.XML_TAG_NAME.equals(region.getType())) {
                            return region.getStart() + start.getStartOffset();
                        }
                    }
                }
            }

            return -1;
        }

        /**
         * 取得&lt;/prefix:name的起始偏移量。
         */
        private int getCloseTagOffset() {
            if (currentNode instanceof IDOMElement
                    && ((IDOMElement) currentNode).getEndStructuredDocumentRegion() != null) {
                IStructuredDocumentRegion end = ((IDOMElement) currentNode).getEndStructuredDocumentRegion();
                ITextRegionList regions = end.getRegions();

                if (regions != null) {
                    for (Iterator<?> i = regions.iterator(); i.hasNext();) {
                        ITextRegion region = (ITextRegion) i.next();

                        if (DOMRegionContext.XML_TAG_NAME.equals(region.getType())) {
                            return region.getStart() + end.getStartOffset();
                        }
                    }
                }
            }

            return -1;
        }

        private boolean isCursorWithin(int startOffset, String content) {
            if (content != null) {
                return currentOffset >= startOffset && currentOffset < (startOffset + content.length());
            } else {
                return false;
            }
        }
    }
}
