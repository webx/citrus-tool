package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant.*;
import static com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom.DomDocumentUtil.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

/**
 * 从文档中添加、删除指定schema的namepsaces。
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
abstract class AbstractAddRemoveNamespaceVisitor extends DocumentVisitor {
    protected final SchemaResourceSet schemas;
    protected IDOMAttr xmlnsXsi;
    protected List<IDOMAttr> otherAttrs = createLinkedList();
    protected NamespaceDefinitions defs = new NamespaceDefinitions();
    protected Set<String> existingPrefixes = createHashSet();
    protected Set<String> namespacesInUse = createHashSet();

    public AbstractAddRemoveNamespaceVisitor(SchemaResourceSet schemas) {
        this.schemas = schemas;
    }

    @Override
    protected void createRootElement(IDOMDocument document) {
        NodeList nodes = document.getChildNodes();
        ProcessingInstruction pi = null;

        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i) instanceof ProcessingInstruction) {
                    pi = (ProcessingInstruction) nodes.item(i);
                }
            }
        }

        if (pi == null) {
            pi = document.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\"");
            document.appendChild(pi);
        }

        Element rootElement = document.createElementNS(SPRING_BEANS_NS, "beans:beans");
        rootElement.setAttribute("xmlns:beans", SPRING_BEANS_NS);
        document.appendChild(rootElement);
    }

    @Override
    protected void visitRootElement() {
        parseSchemaLocations();
        saveNamespace(getCurrentElement());
        visitChildren();
        visitAttributes();

        if (xmlnsXsi != null) {
            getCurrentElement().removeAttributeNode(xmlnsXsi);
        }

        updateDocument();
    }

    @Override
    protected void visitElement() {
        parseSchemaLocations();
        saveNamespace(getCurrentElement());
        visitChildren();
        visitAttributes();
    }

    @Override
    protected void visitRootElementAttribute() {
        saveNamespace(getCurrentAttribute());

        String nsPrefix = getNamespacePrefix(getCurrentAttribute());

        if (nsPrefix != null) {
            String namespace = trimToEmpty(getCurrentAttribute().getNodeValue());

            // xmlns:schemaNs
            if (schemas.getNamespaceMappings().containsKey(namespace)) {
                defs.add(new NamespaceDefinition(namespace, nsPrefix, getSchemaLocations()));
                getCurrentElement().removeAttributeNode(getCurrentAttribute());
            }

            // xmlns:xsi
            else if (namespace.equals(NS_XSI)) {
                xmlnsXsi = getCurrentAttribute();
            }

            // other attrs
            else {
                otherAttrs.add(getCurrentAttribute());
                getCurrentElement().removeAttributeNode(getCurrentAttribute());
            }

            existingPrefixes.add(nsPrefix);
        }

        // xsi:schemaLocation
        else if (NS_XSI.equals(getCurrentAttribute().getNamespaceURI())
                && ATTR_SCHEMA_LOCATION.equals(getCurrentAttribute().getLocalName())) {
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

        // xsi:schemaLocation
        if (NS_XSI.equals(getCurrentAttribute().getNamespaceURI())
                && ATTR_SCHEMA_LOCATION.equals(getCurrentAttribute().getLocalName())) {
            getCurrentElement().removeAttributeNode(getCurrentAttribute());
        }
    }

    private void saveNamespace(Node node) {
        String elementNs = trimToNull(node.getNamespaceURI());

        if (elementNs != null) {
            namespacesInUse.add(elementNs);
        }
    }

    private void updateDocument() {
        String locationPrefix = guessLocationPrefix(getSchemaLocations(), schemas);

        // 排序并加回所有的attrs
        // 1. xmlns:xsi
        String xsiPrefix = xmlnsXsi == null ? "xsi" : getNamespacePrefix(xmlnsXsi);
        getCurrentElement().setAttribute("xmlns:" + xsiPrefix, NS_XSI);

        // 2. namespace definitions
        updateNamespaces();

        processSchemaLocations(locationPrefix);

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
        getCurrentElement().setAttribute(xsiPrefix + ":" + ATTR_SCHEMA_LOCATION,
                formatSchemaLocations(getSchemaLocations(), getCurrentElement().getNodeName()));

        // 5. other  attrs
        for (IDOMAttr attr : otherAttrs) {
            if (!attr.getNodeName().startsWith("xmlns")) {
                getCurrentElement().setAttribute(attr.getNodeName(), attr.getNodeValue());
            }
        }
    }

    protected abstract void updateNamespaces();

    /**
     * 将缺少的schemaLocation补全。
     */
    private void processSchemaLocations(String locationPrefix) {
        for (String ns : defs.getNamespaces()) {
            if (!getSchemaLocations().containsKey(ns)) {
                Schema s = schemas.findSchemaByUrl(ns);

                if (s != null) {
                    getSchemaLocations().put(ns, locationPrefix + s.getName());
                }
            }
        }
    }
}
