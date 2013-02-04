package com.alibaba.ide.plugin.eclipse.springext.util.dom;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfig;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

@SuppressWarnings("restriction")
public class DomDocumentUtil {
    public static void removeUnusedNamespaceDefinitions(SpringExtConfig config) {
        StructuredTextViewer textViewer = config.getTextViewer();
        IDOMDocument document = config.getDomDocument();
        SchemaResourceSet schemas = config.getSchemas();

        document.getModel().beginRecording(textViewer);

        DocumentVisitor visitor = new RemoveUnusedNamespacesVisitor(schemas);

        try {
            visitor.accept(document);
        } finally {
            document.getModel().endRecording(textViewer);
        }
    }

    public static void updateNamespaceDefinition(SpringExtConfig config, NamespaceItem item, boolean checked) {
        StructuredTextViewer textViewer = config.getTextViewer();
        IDOMDocument document = config.getDomDocument();
        SchemaResourceSet schemas = config.getSchemas();
        String namespaceToUpdate = item.getNamespace();

        document.getModel().beginRecording(textViewer);

        DocumentVisitor visitor = checked ? new AddNamespaceVisitor(schemas, namespaceToUpdate)
                : new RemoveNamespaceVisitor(schemas, namespaceToUpdate);

        try {
            visitor.accept(document);
        } finally {
            document.getModel().endRecording(textViewer);
        }
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

    public static class NamespaceDefinitions {
        private final Map<String/* ns */, Map<String/* prefix */, NamespaceDefinition>> namespaces = createLinkedHashMap();

        public void add(NamespaceDefinition nd) {
            Map<String, NamespaceDefinition> prefixes = namespaces.get(nd.getNamespace());

            if (prefixes == null) {
                prefixes = createHashMap();
                namespaces.put(nd.getNamespace(), prefixes);
            }

            prefixes.put(nd.getPrefix(), nd);
        }

        public void remove(String namespace) {
            namespaces.remove(namespace);
        }

        public Map<String, NamespaceDefinition> find(String namespace) {
            Map<String, NamespaceDefinition> prefixes = namespaces.get(trimToNull(namespace));

            if (prefixes == null) {
                return emptyMap();
            } else {
                return prefixes;
            }
        }

        public String[] getNamespaces() {
            return namespaces.keySet().toArray(new String[namespaces.size()]);
        }
    }

    public static class NamespaceDefinition {
        private final String namespace;
        private final String prefix;
        private final String location;

        public NamespaceDefinition(String namespace, String prefix, Map<String, String> schemaLocations) {
            this.namespace = trimToNull(namespace);
            this.prefix = trimToNull(prefix);
            this.location = schemaLocations.get(this.namespace);
        }

        public String getNamespace() {
            return namespace;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return "xmlns" + (prefix == null ? "" : ":" + prefix) + "=\"" + namespace + "\"";
        }
    }
}
