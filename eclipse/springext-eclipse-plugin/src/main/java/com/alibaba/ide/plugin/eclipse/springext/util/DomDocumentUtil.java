package com.alibaba.ide.plugin.eclipse.springext.util;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.Map;
import java.util.Set;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfig;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

@SuppressWarnings("restriction")
public class DomDocumentUtil {
    public static void updateNamespaceDefinition(SpringExtConfig config, NamespaceItem item, boolean checked) {
        StructuredTextViewer textViewer = config.getTextViewer();
        IDOMDocument document = config.getDomDocument();
        SchemaResourceSet schemas = config.getSchemas();
        String namespaceToUpdate = item.getNamespace();

        Set<Schema> schemasOfNs = schemas.getNamespaceMappings().get(namespaceToUpdate);
        Schema mainSchema = null;

        if (schemasOfNs != null && !schemasOfNs.isEmpty()) {
            mainSchema = schemasOfNs.iterator().next();
        }

        if (mainSchema != null) {
            Schema schema = mainSchema;

            document.getModel().beginRecording(textViewer);

            try {
                new AddRemoveNamespaceVisitor(schemas, schema, checked).visit(document);
            } finally {
                document.getModel().endRecording(textViewer);
            }
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
                String nsPrefix = getNamespacePrefix();

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

        finder.visit(document);

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
                String nsPrefix = getNamespacePrefix();

                if (nsPrefix != null) {
                    defs.add(new NamespaceDefinition(getCurrentAttribute().getNodeValue(), nsPrefix,
                            getSchemaLocations()));
                }
            }
        }.visit(document);

        return defs;
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
