package com.alibaba.ide.plugin.eclipse.springext.util;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.lang.Math.*;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.w3c.dom.Node;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SchemaUtil;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;
import com.alibaba.ide.plugin.eclipse.springext.util.DomDocumentUtil.NamespaceDefinition;
import com.alibaba.ide.plugin.eclipse.springext.util.DomDocumentUtil.NamespaceDefinitions;

/**
 * 从文档中添加、删除指定schema的namepsaces。
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
class AddRemoveNamespaceVisitor extends DocumentVisitor {
    private final static FormatProcessorXML formatter = new FormatProcessorXML();
    private final SchemaResourceSet schemas;
    private final Schema schema;
    private final boolean checked;
    private String xsiPrefix;
    private List<IDOMAttr> otherAttrs = createLinkedList();
    private NamespaceDefinitions defs = new NamespaceDefinitions();
    private Set<String> existingPrefixes = createHashSet();
    private Set<String> namespacesInUse = createHashSet();

    public AddRemoveNamespaceVisitor(SchemaResourceSet schemas, Schema schema, boolean checked) {
        this.schemas = schemas;
        this.schema = schema;
        this.checked = checked;
    }

    @Override
    protected void visitRootElement() {
        parseSchemaLocations();
        saveNamespace(getCurrentElement());
        visitChildren();
        visitAttributes();
        updateDocument(schema, checked);
    }

    @Override
    protected void visitElement() {
        saveNamespace(getCurrentElement());
        visitChildren();
        visitAttributes();
    }

    @Override
    protected void visitRootElementAttribute() {
        saveNamespace(getCurrentAttribute());

        String nsPrefix = getNamespacePrefix();

        if (nsPrefix != null) {
            String namespace = trimToEmpty(getCurrentAttribute().getNodeValue());

            // xmlns:schemaNs
            if (schemas.getNamespaceMappings().containsKey(namespace)) {
                defs.add(new NamespaceDefinition(namespace, nsPrefix, getSchemaLocations()));
                getCurrentElement().removeAttributeNode(getCurrentAttribute());
            }

            // xmlns:xsi
            else if (namespace.equals(NS_XSI)) {
                getCurrentElement().removeAttributeNode(getCurrentAttribute());
                xsiPrefix = nsPrefix;
            }

            // other attrs
            else {
                otherAttrs.add(getCurrentAttribute());
                getCurrentElement().removeAttributeNode(getCurrentAttribute());
            }

            existingPrefixes.add(nsPrefix);
        }

        // xsi:schemaLocation
        else if ((NS_XSI.equals(getCurrentAttribute().getNamespaceURI()) && ATTR_SCHEMA_LOCATION
                .equals(getCurrentAttribute().getLocalName()))) {
            getCurrentElement().removeAttributeNode(getCurrentAttribute());
        }

        // xsi:schemaLocation after removal of xmlns:xsi
        else if (xsiPrefix != null
                && (xsiPrefix + ":" + ATTR_SCHEMA_LOCATION).equals(getCurrentAttribute().getNodeName())) {
            getCurrentElement().removeAttributeNode(getCurrentAttribute());
        }

        // other attrs
        else {
            otherAttrs.add(getCurrentAttribute());
            getCurrentElement().removeAttributeNode(getCurrentAttribute());
        }
    }

    @Override
    protected void visitAttribute() {
        saveNamespace(getCurrentAttribute());
    }

    private void saveNamespace(Node node) {
        String elementNs = trimToNull(node.getNamespaceURI());

        if (elementNs != null) {
            namespacesInUse.add(elementNs);
        }
    }

    private void updateDocument(final Schema schema, final boolean checked) {
        String locationPrefix = getLocationPrefix();

        // 排序并加回所有的attrs
        // 1. xmlns:xsi
        getCurrentElement().setAttribute("xmlns:" + xsiPrefix, NS_XSI);

        // 2. namespace definitions
        if (checked) {
            String nsPrefixBase = SchemaUtil.getNamespacePrefix(schema.getPreferredNsPrefix(),
                    schema.getTargetNamespace());

            String nsPrefix = nsPrefixBase;

            // 避免prefix重复。
            for (int i = 1; existingPrefixes.contains(nsPrefix); i++) {
                nsPrefix = nsPrefixBase + i;
            }

            defs.add(new NamespaceDefinition(schema.getTargetNamespace(), nsPrefix, getSchemaLocations()));

            getSchemaLocations().put(schema.getTargetNamespace(), locationPrefix + schema.getName());
        } else {
            String nsToRemove = schema.getTargetNamespace();

            // 避免删除正在使用的namespace
            if (!namespacesInUse.contains(nsToRemove)) {
                defs.remove(nsToRemove);
                getSchemaLocations().remove(nsToRemove);
            }
        }

        String[] namespaces = defs.getNamespaces();

        Arrays.sort(namespaces);

        for (String ns : namespaces) {
            Map<String, NamespaceDefinition> mappings = defs.find(ns);
            String[] prefixes = mappings.keySet().toArray(new String[mappings.size()]);

            Arrays.sort(prefixes);

            for (String prefix : prefixes) {
                if (isEmpty(prefix)) {
                    getCurrentElement().setAttribute("xmlns", mappings.get(prefix).getNamespace());
                } else {
                    getCurrentElement().setAttribute("xmlns:" + prefix, mappings.get(prefix).getNamespace());
                }
            }
        }

        // 3. other xmlns attrs
        for (IDOMAttr attr : otherAttrs) {
            if (attr.getNodeName().startsWith("xmlns")) {
                getCurrentElement().setAttribute(attr.getNodeName(), attr.getNodeValue());
            }
        }

        // 4. schema locations
        getCurrentElement().setAttribute(xsiPrefix + ":" + ATTR_SCHEMA_LOCATION, generateSchemaLocation());

        // 5. other  attrs
        for (IDOMAttr attr : otherAttrs) {
            if (!attr.getNodeName().startsWith("xmlns")) {
                getCurrentElement().setAttribute(attr.getNodeName(), attr.getNodeValue());
            }
        }

        formatter.formatNode(getCurrentElement());
    }

    private String generateSchemaLocation() {
        StringBuilder buf = new StringBuilder();
        Formatter formatter = new Formatter(buf);

        try {
            String prefix = getCurrentElement().getNodeName().replaceAll(".", " ") + "  ";
            String indent = "    ";

            formatter.format("%n");

            int maxLength = 0;

            for (String ns : getSchemaLocations().keySet()) {
                maxLength = max(maxLength, ns.length());
            }

            String format = "%s%s%-" + maxLength + "s %s%n";

            for (Map.Entry<String, String> entry : getSchemaLocations().entrySet()) {
                String ns = entry.getKey();
                String location = entry.getValue();

                formatter.format(format, prefix, indent, ns, location);
            }

            formatter.format(prefix);
        } finally {
            formatter.close();
        }

        return buf.toString();
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
                        locationPrefix = location.substring(0, location.length() - schema.getName().length());
                        break;
                    }
                }
            }
        }

        if (locationPrefix == null || locationPrefix.equals("http:") || locationPrefix.equals("http://")) {
            locationPrefix = "http://localhost:8080/schema/";
        }

        return locationPrefix;
    }
}
