package com.alibaba.ide.plugin.eclipse.springext.util;

import static com.alibaba.citrus.util.StringUtil.*;

import java.util.LinkedList;
import java.util.Map;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;

@SuppressWarnings("restriction")
public class SpringExtConfigUtil {
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
                    String namespace = trimToEmpty(attribute.getNodeValue());

                    if (namespace.equals(namespaceToFind)) {
                        nd = new NamespaceDefinition(attribute.getNodeValue(), nsPrefix, getSchemaLocations());
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
                String nsPrefix = getNamespacePrefix();

                if (nsPrefix != null) {
                    defs.add(new NamespaceDefinition(attribute.getNodeValue(), nsPrefix, getSchemaLocations()));
                }
            }
        }.accept(document);

        return defs;
    }

    public static class NamespaceDefinitions extends LinkedList<NamespaceDefinition> {
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
