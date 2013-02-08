package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.dom;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DOMWriter;
import org.eclipse.wst.sse.core.internal.format.IStructuredFormatPreferences;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.w3c.dom.Node;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;
import com.alibaba.citrus.springext.util.ConvertToUnqualifiedStyle.Converter;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfig;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

@SuppressWarnings("restriction")
public class DomDocumentUtil {
    private final static FormatProcessorXML formatter = new FormatProcessorXML() {
        @Override
        public IStructuredFormatPreferences getFormatPreferences() {
            IStructuredFormatPreferences prefs = super.getFormatPreferences();

            if (prefs.getLineWidth() < 120) {
                prefs.setLineWidth(120);
            }

            return prefs;
        }
    };

    public static void upgradeToUnqualifiedStyle(SpringExtConfig config) {
        StructuredTextViewer textViewer = config.getTextViewer();
        SchemaResourceSet schemas = config.getSchemas();
        final IDOMDocument document = config.getDomDocument();

        class MyDOMReader extends DOMReader {
            @Override
            protected void readTree(Node node, Branch current) {
                if (current instanceof Document) {
                    // 只解析root element，避免Dom4j NPE
                    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        super.readTree(node, current);
                    }
                } else {
                    super.readTree(node, current);
                }
            }
        }

        Document dom4jDoc = new MyDOMReader().read(document);

        new Converter(dom4jDoc, schemas).convert();

        document.getModel().beginRecording(textViewer);

        try {
            LinkedList<Node> nodesAfterRootElement = createLinkedList();

            for (Node node = document.getDocumentElement(); node != null; node = node.getNextSibling()) {
                nodesAfterRootElement.add(node);
            }

            for (Node node : nodesAfterRootElement) {
                document.removeChild(node);
            }

            nodesAfterRootElement.removeFirst();

            class MyDOMWriter extends DOMWriter {
                public void appendToW3CDoc(Document dom4jDoc) {
                    appendDOMTree(document, document, dom4jDoc.content());
                }

                @Override
                protected void appendDOMTree(org.w3c.dom.Document domDocument, org.w3c.dom.Node domCurrent,
                                             Element element) {
                    super.appendDOMTree(domDocument, domCurrent, element);

                    // 将无内容的element设置成empty tag。
                    if (!element.hasContent() && domCurrent.getLastChild() instanceof IDOMElement) {
                        ((IDOMElement) domCurrent.getLastChild()).setEmptyTag(true);
                    }
                }
            }

            new MyDOMWriter().appendToW3CDoc(dom4jDoc);

            for (Node node : nodesAfterRootElement) {
                document.appendChild(node);
            }

            format(document);
        } finally {
            document.getModel().endRecording(textViewer);
        }
    }

    public static void cleanupUnusedNamespaceDefinitions(SpringExtConfig config) {
        StructuredTextViewer textViewer = config.getTextViewer();
        IDOMDocument document = config.getDomDocument();
        SchemaResourceSet schemas = config.getSchemas();

        document.getModel().beginRecording(textViewer);

        DocumentVisitor visitor = new CleanupUnusedNamespacesVisitor(schemas);

        try {
            visitor.accept(document);
            format(document);
        } finally {
            document.getModel().endRecording(textViewer);
        }
    }

    public static void updateNamespaceDefinitionLocation(SpringExtConfig config, Schema schema, boolean checked) {
        StructuredTextViewer textViewer = config.getTextViewer();
        IDOMDocument document = config.getDomDocument();
        SchemaResourceSet schemas = config.getSchemas();

        document.getModel().beginRecording(textViewer);

        DocumentVisitor visitor = checked ? new ChangeSchemaLocationVisitor(schemas, schema)
                : new RemoveNamespaceVisitor(schemas, schema.getTargetNamespace());

        try {
            visitor.accept(document);
            format(document);
        } finally {
            document.getModel().endRecording(textViewer);
        }
    }

    public static void updateNamespaceDefinitions(SpringExtConfig config, TreeItem item, boolean checked) {
        StructuredTextViewer textViewer = config.getTextViewer();
        IDOMDocument document = config.getDomDocument();
        SchemaResourceSet schemas = config.getSchemas();
        List<String> namespacesToUpdate = createLinkedList();

        if (item instanceof NamespaceItem) {
            namespacesToUpdate.add(((NamespaceItem) item).getNamespace());
        } else {
            for (TreeItem child : item.getChildren()) {
                if (child instanceof NamespaceItem) {
                    namespacesToUpdate.add(((NamespaceItem) child).getNamespace());
                }
            }
        }

        String[] namespaces = namespacesToUpdate.toArray(new String[namespacesToUpdate.size()]);

        document.getModel().beginRecording(textViewer);

        DocumentVisitor visitor = checked ? new AddNamespaceVisitor(schemas, namespaces) : new RemoveNamespaceVisitor(
                schemas, namespaces);

        try {
            visitor.accept(document);
            format(document);
        } finally {
            document.getModel().endRecording(textViewer);
        }
    }

    private static void format(IDOMDocument document) {
        formatter.formatNode(document.getDocumentElement());
    }

    public static NamespaceDefinition getNamespace(IDOMDocument document, final String namespaceToFind) {
        class NamespaceFinder extends DocumentVisitor {
            NamespaceDefinition nd;

            @Override
            protected void visitElement() {
                parseSchemaLocations();
                visitAttributes();
                visitChildren();
            }

            @Override
            protected void visitAttribute() {
                String nsPrefix = getNamespacePrefix(getCurrentAttribute());

                if (nsPrefix != null) {
                    String namespace = trimToEmpty(getCurrentAttribute().getNodeValue());

                    if (namespace.equals(namespaceToFind)) {
                        nd = new NamespaceDefinition(namespace, nsPrefix, getSchemaLocations());
                    }
                }
            }

            @Override
            protected boolean continueToNextAttribute() {
                return nd == null;
            }

            @Override
            protected boolean continueToNextChild() {
                return nd == null;
            }
        }

        NamespaceFinder finder = new NamespaceFinder();

        finder.accept(document);

        return finder.nd;
    }

    /**
     * 从dom中取得所有的namespaces和schemaLocations。
     */
    public static NamespaceDefinitions loadNamespaces(IDOMDocument document) {
        final NamespaceDefinitions defs = new NamespaceDefinitions();

        new DocumentVisitor() {
            @Override
            protected void visitElement() {
                parseSchemaLocations();

                visitAttributes();
                visitChildren();
            }

            @Override
            protected void visitAttribute() {
                String nsPrefix = getNamespacePrefix(getCurrentAttribute());

                if (nsPrefix != null) {
                    defs.add(new NamespaceDefinition(getCurrentAttribute().getNodeValue(), nsPrefix,
                            getSchemaLocations()));
                }
            }
        }.accept(document);

        return defs;
    }

    private final static Pattern XMLNS_PATTERN = Pattern.compile("xmlns(:(.*))?", Pattern.CASE_INSENSITIVE);

    /**
     * 如果指定的attribute是一个xmlns定义，则返回其namespace前缀；如果不是，则返回<code>null</code>。
     */
    public static String getNamespacePrefix(IDOMAttr attr) {
        String attrName = trimToEmpty(attr.getNodeName());
        Matcher m = XMLNS_PATTERN.matcher(attrName);

        if (m.matches()) {
            return trimToEmpty(m.group(2));
        }

        return null;
    }
}
