package com.alibaba.ide.plugin.eclipse.springext.util;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.citrus.springext.support.SchemaUtil;

/**
 * 用来遍历dom的visitor。
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
public abstract class DocumentVisitor {
    private final Map<String, String> schemaLocations = createTreeMap();
    protected IDOMElement element;
    protected IDOMAttr attribute;

    public void accept(IDOMDocument document) {
        Element rootElement = document == null ? null : document.getDocumentElement();

        if (rootElement instanceof IDOMElement) {
            element = (IDOMElement) rootElement;
            visitElement();
        }
    }

    public Map<String, String> getSchemaLocations() {
        return schemaLocations;
    }

    protected void visitElement() {
    }

    protected void visitAttribute() {
    }

    protected boolean continueToNextAttribute() {
        return true;
    }

    protected boolean continueToNextChild() {
        return true;
    }

    protected final void visitChildren() {
        NodeList children = element.getChildNodes();
        List<IDOMElement> elements = createLinkedList();

        // 复制elements，以便子类可以修改当前的element。
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node instanceof IDOMElement) {
                elements.add((IDOMElement) node);
            }
        }

        for (IDOMElement node : elements) {
            IDOMElement currentElement = element;

            try {
                element = node;
                visitElement();

                if (!continueToNextChild()) {
                    break;
                }
            } finally {
                element = currentElement;
            }
        }
    }

    protected final void visitAttributes() {
        NamedNodeMap attrs = element.getAttributes();
        List<IDOMAttr> attrList = createLinkedList();

        // 复制attributes，以便子类可以修改当前的attribute。
        for (int i = 0; i < attrs.getLength(); i++) {
            Node node = attrs.item(i);

            if (node instanceof IDOMAttr) {
                attrList.add((IDOMAttr) node);
            }
        }

        for (IDOMAttr node : attrList) {
            IDOMAttr currentAttr = attribute;

            try {
                attribute = (IDOMAttr) node;
                visitAttribute();

                if (!continueToNextAttribute()) {
                    break;
                }
            } finally {
                attribute = currentAttr;
            }
        }
    }

    /**
     * 如果attr是一个xmlns定义，则返回namespace前缀。
     */
    protected final String getNamespacePrefix() {
        String attrName = trimToEmpty(attribute.getNodeName());
        Matcher m = XMLNS_PATTERN.matcher(attrName);

        if (m.matches()) {
            return trimToEmpty(m.group(2));
        }

        return null;
    }

    protected final static String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    protected final static String ATTR_SCHEMA_LOCATION = "schemaLocation";
    private final static Pattern XMLNS_PATTERN = Pattern.compile("xmlns(:(.*))?", Pattern.CASE_INSENSITIVE);

    protected final void parseSchemaLocations() {
        String schemaLocationValue = element.getAttributeNS(NS_XSI, ATTR_SCHEMA_LOCATION);

        for (Map.Entry<String, String> entry : SchemaUtil.parseSchemaLocation(schemaLocationValue).entrySet()) {
            String namespace = entry.getKey();
            String location = entry.getValue();

            if (!schemaLocations.containsKey(namespace)) {
                schemaLocations.put(namespace, location);
            }
        }
    }
}
