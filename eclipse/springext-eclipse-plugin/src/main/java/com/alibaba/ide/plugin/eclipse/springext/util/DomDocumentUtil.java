package com.alibaba.ide.plugin.eclipse.springext.util;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SchemaUtil;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfig;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

@SuppressWarnings("restriction")
public class DomDocumentUtil {
    private static FormatProcessorXML formatter = new FormatProcessorXML();

    public static void updateNamespaceDefinition(SpringExtConfig config, NamespaceItem item, final boolean checked) {
        StructuredTextViewer textViewer = config.getTextViewer();
        IDOMDocument document = config.getDomDocument();
        final SchemaResourceSet schemas = config.getSchemas();
        String namespaceToUpdate = item.getNamespace();

        Set<Schema> schemasOfNs = schemas.getNamespaceMappings().get(namespaceToUpdate);
        Schema mainSchema = null;

        if (schemasOfNs != null && !schemasOfNs.isEmpty()) {
            mainSchema = schemasOfNs.iterator().next();
        }

        if (mainSchema != null) {
            final Schema schema = mainSchema;

            document.getModel().beginRecording(textViewer);

            try {
                new DocumentVisitor() {
                    private String xsiPrefix;
                    private List<IDOMAttr> otherAttrs = createLinkedList();
                    private NamespaceDefinitions defs = new NamespaceDefinitions();

                    @Override
                    protected void visitElement() {
                        parseSchemaLocations();
                        visitAttributes();

                        String locationPrefix = getLocationPrefix();

                        // 排序并加回所有的attrs
                        // 1. xmlns:xsi
                        element.setAttribute("xmlns:" + xsiPrefix, NS_XSI);

                        // 2. namespace definitions
                        if (checked) {
                            String nsPrefix = SchemaUtil.getNamespacePrefix(schema.getPreferredNsPrefix(),
                                    schema.getTargetNamespace());

                            defs.add(new NamespaceDefinition(schema.getTargetNamespace(), nsPrefix,
                                    getSchemaLocations()));

                            getSchemaLocations().put(schema.getTargetNamespace(), locationPrefix + schema.getName());
                        } else {
                            defs.remove(schema.getTargetNamespace());
                            getSchemaLocations().remove(schema.getTargetNamespace());
                        }

                        String[] namespaces = defs.getNamespaces();

                        Arrays.sort(namespaces);

                        for (String ns : namespaces) {
                            Map<String, NamespaceDefinition> mappings = defs.find(ns);
                            String[] prefixes = mappings.keySet().toArray(new String[mappings.size()]);

                            Arrays.sort(prefixes);

                            for (String prefix : prefixes) {
                                if (isEmpty(prefix)) {
                                    element.setAttribute("xmlns", mappings.get(prefix).getNamespace());
                                } else {
                                    element.setAttribute("xmlns:" + prefix, mappings.get(prefix).getNamespace());
                                }
                            }
                        }

                        // 3. other xmlns attrs
                        for (IDOMAttr attr : otherAttrs) {
                            if (attr.getNodeName().startsWith("xmlns")) {
                                element.setAttribute(attr.getNodeName(), attr.getNodeValue());
                            }
                        }

                        // 4. schema locations
                        StringBuilder buf = new StringBuilder();

                        String prefix = element.getNodeName().replaceAll(".", " ") + "  ";
                        String indent = "    ";

                        buf.append("\n");

                        for (Map.Entry<String, String> entry : getSchemaLocations().entrySet()) {
                            String ns = entry.getKey();
                            String location = entry.getValue();

                            buf.append(prefix).append(indent).append(ns).append(" ").append(location).append("\n");
                        }

                        buf.append(prefix);

                        element.setAttribute(xsiPrefix + ":" + ATTR_SCHEMA_LOCATION, buf.toString());

                        // 5. other  attrs
                        for (IDOMAttr attr : otherAttrs) {
                            if (!attr.getNodeName().startsWith("xmlns")) {
                                element.setAttribute(attr.getNodeName(), attr.getNodeValue());
                            }
                        }

                        formatter.formatNode(element);
                    }

                    private String getLocationPrefix() {
                        String locationPrefix = null;

                        for (Map.Entry<String, String> entry : getSchemaLocations().entrySet()) {
                            String uri = entry.getKey();
                            String location = entry.getValue();
                            Set<Schema> schemaSet = schemas.getNamespaceMappings().get(uri);

                            if (schemaSet != null) {
                                for (Schema schema : schemaSet) {
                                    if (location.endsWith(schema.getName())) {
                                        locationPrefix = location.substring(0, location.length()
                                                - schema.getName().length());
                                        break;
                                    }
                                }
                            }
                        }

                        if (locationPrefix == null || locationPrefix.equals("http:")
                                || locationPrefix.equals("http://")) {
                            locationPrefix = "http://localhost:8080/schema/";
                        }

                        return locationPrefix;
                    }

                    @Override
                    protected void visitAttribute() {
                        String nsPrefix = getNamespacePrefix();

                        if (nsPrefix != null) {
                            String namespace = trimToEmpty(attribute.getNodeValue());

                            // xmlns:schemaNs
                            if (schemas.getNamespaceMappings().containsKey(namespace)) {
                                defs.add(new NamespaceDefinition(namespace, nsPrefix, getSchemaLocations()));
                                element.removeAttributeNode(attribute);
                            }

                            // xmlns:xsi
                            else if (namespace.equals(NS_XSI)) {
                                element.removeAttributeNode(attribute);
                                xsiPrefix = nsPrefix;
                            }

                            // other attrs
                            else {
                                otherAttrs.add(attribute);
                                element.removeAttributeNode(attribute);
                            }
                        }

                        // xsi:schemaLocation
                        else if ((NS_XSI.equals(attribute.getNamespaceURI()) && ATTR_SCHEMA_LOCATION.equals(attribute
                                .getLocalName()))) {
                            element.removeAttributeNode(attribute);
                        }

                        // xsi:schemaLocation after removal of xmlns:xsi
                        else if (xsiPrefix != null
                                && (xsiPrefix + ":" + ATTR_SCHEMA_LOCATION).equals(attribute.getNodeName())) {
                            element.removeAttributeNode(attribute);
                        }

                        // other attrs
                        else {
                            otherAttrs.add(attribute);
                            element.removeAttributeNode(attribute);
                        }
                    }

                    @Override
                    protected boolean continueToNextChild() {
                        return false;
                    }

                    @Override
                    protected boolean continueToNextAttribute() {
                        return true;
                    }
                }.accept(document);
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
                    String namespace = trimToEmpty(attribute.getNodeValue());

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
                String nsPrefix = getNamespacePrefix();

                if (nsPrefix != null) {
                    defs.add(new NamespaceDefinition(attribute.getNodeValue(), nsPrefix, getSchemaLocations()));
                }
            }
        }.accept(document);

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
