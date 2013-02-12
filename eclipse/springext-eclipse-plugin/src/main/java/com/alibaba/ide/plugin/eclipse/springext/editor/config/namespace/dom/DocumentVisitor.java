package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.List;
import java.util.Map;

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
abstract class DocumentVisitor {
    public final static String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public final static String ATTR_SCHEMA_LOCATION = "schemaLocation";
    private final Map<String, String> schemaLocations = createTreeMap();
    private Element rootElement;
    private IDOMElement currentElement;
    private IDOMAttr currentAttribute;

    public void accept(IDOMDocument document) {
        if (document != null) {
            if (document.getDocumentElement() == null) {
                createRootElement(document);
            }

            rootElement = document.getDocumentElement();

            if (rootElement instanceof IDOMElement) {
                currentElement = (IDOMElement) rootElement;
                visitRootElement();
            }
        }
    }

    protected void createRootElement(IDOMDocument document) {
    }

    protected void visitRootElement() {
        visitElement();
    }

    protected void visitElement() {
    }

    protected void visitRootElementAttribute() {
        visitAttribute();
    }

    protected void visitAttribute() {
    }

    protected boolean continueToNextChild() {
        return true;
    }

    protected boolean continueToNextAttribute() {
        return true;
    }

    public final Element getRootElement() {
        return rootElement;
    }

    public final IDOMElement getCurrentElement() {
        return currentElement;
    }

    public final IDOMAttr getCurrentAttribute() {
        return currentAttribute;
    }

    public final Map<String, String> getSchemaLocations() {
        return schemaLocations;
    }

    /**
     * 子类调用此方法，可递归遍历所有子elements。
     * <p/>
     * 对于每一个element，<code>visitElement()</code>方法将被调用。
     * <p/>
     * 如果<code>continueToNextChild()</code>方法返回false，则遍历被中断，下一个element将不被访问到。
     */
    protected final void visitChildren() {
        NodeList children = currentElement.getChildNodes();
        List<IDOMElement> elements = createLinkedList();

        // 复制elements，以便子类可以修改当前的element。
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node instanceof IDOMElement) {
                elements.add((IDOMElement) node);
            }
        }

        for (IDOMElement node : elements) {
            IDOMElement currentElementBackup = currentElement;

            try {
                currentElement = node;
                visitElement();

                if (!continueToNextChild()) {
                    break;
                }
            } finally {
                currentElement = currentElementBackup;
            }
        }
    }

    /**
     * 子类调用此方法，可递归当前element中的所有attributes。
     * <p/>
     * 对于每一个attribute，<code>visitAttribute()</code>方法将被调用。
     * <p/>
     * 如果<code>continueToNextAttribute()</code>
     * 方法返回false，则遍历被中断，下一个attribute将不被访问到。
     */
    protected final void visitAttributes() {
        NamedNodeMap attrs = currentElement.getAttributes();
        List<IDOMAttr> attrList = createLinkedList();
        boolean isRootElement = currentElement == rootElement;

        // 复制attributes，以便子类可以修改当前的attribute。
        for (int i = 0; i < attrs.getLength(); i++) {
            Node node = attrs.item(i);

            if (node instanceof IDOMAttr) {
                attrList.add((IDOMAttr) node);
            }
        }

        for (IDOMAttr node : attrList) {
            IDOMAttr currentAttr = currentAttribute;

            try {
                currentAttribute = node;

                if (isRootElement) {
                    visitRootElementAttribute();
                } else {
                    visitAttribute();
                }

                if (!continueToNextAttribute()) {
                    break;
                }
            } finally {
                currentAttribute = currentAttr;
            }
        }
    }

    /**
     * 如果当前的element中包含了schemaLocation，则解析它。通过<code>getSchemaLocations()</code>
     * 可以取得结果。
     */
    protected final void parseSchemaLocations() {
        String schemaLocationValue = currentElement.getAttributeNS(NS_XSI, ATTR_SCHEMA_LOCATION);

        for (Map.Entry<String, String> entry : SchemaUtil.parseSchemaLocation(schemaLocationValue).entrySet()) {
            String namespace = entry.getKey();
            String location = entry.getValue();

            if (!schemaLocations.containsKey(namespace)) {
                schemaLocations.put(namespace, location);
            }
        }
    }
}
