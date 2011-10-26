/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * @(#)DOMElementImpl.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

/**
 * DOMElementImpl (c) 1998-2000 (W3C) MIT, INRIA, Keio University See Tidy.java
 * for the copyright notice. Derived from <a
 * href="http://www.w3.org/People/Raggett/tidy"> HTML Tidy Release 4 Aug
 * 2000</a>
 * 
 * @author Dave Raggett <dsr@w3.org>
 * @author Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.4, 1999/09/04 DOM Support
 * @version 1.5, 1999/10/23 Tidy Release 27 Sep 1999
 * @version 1.6, 1999/11/01 Tidy Release 22 Oct 1999
 * @version 1.7, 1999/12/06 Tidy Release 30 Nov 1999
 * @version 1.8, 2000/01/22 Tidy Release 13 Jan 2000
 * @version 1.9, 2000/06/03 Tidy Release 30 Apr 2000
 * @version 1.10, 2000/07/22 Tidy Release 8 Jul 2000
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class DOMElementImpl extends DOMNodeImpl implements org.w3c.dom.Element {

    protected DOMElementImpl(Node adaptee) {
        super(adaptee);
    }

    /* --------------------- DOM ---------------------------- */

    /**
     * @see org.w3c.dom.Node#getNodeType
     */
    public short getNodeType() {
        return org.w3c.dom.Node.ELEMENT_NODE;
    }

    /**
     * @see org.w3c.dom.Element#getTagName
     */
    public String getTagName() {
        return super.getNodeName();
    }

    /**
     * @see org.w3c.dom.Element#getAttribute
     */
    public String getAttribute(String name) {
        if (this.adaptee == null)
            return null;

        AttVal att = this.adaptee.attributes;
        while (att != null) {
            if (att.attribute.equals(name))
                break;
            att = att.next;
        }
        if (att != null)
            return att.value;
        else
            return "";
    }

    /**
     * @see org.w3c.dom.Element#setAttribute
     */
    public void setAttribute(String name, String value) throws DOMException {
        if (this.adaptee == null)
            return;

        AttVal att = this.adaptee.attributes;
        while (att != null) {
            if (att.attribute.equals(name))
                break;
            att = att.next;
        }
        if (att != null) {
            att.value = value;
        } else {
            att = new AttVal(null, null, (int) '"', name, value);
            att.dict = AttributeTable.getDefaultAttributeTable().findAttribute(att);
            if (this.adaptee.attributes == null) {
                this.adaptee.attributes = att;
            } else {
                att.next = this.adaptee.attributes;
                this.adaptee.attributes = att;
            }
        }
    }

    /**
     * @see org.w3c.dom.Element#removeAttribute
     */
    public void removeAttribute(String name) throws DOMException {
        if (this.adaptee == null)
            return;

        AttVal att = this.adaptee.attributes;
        AttVal pre = null;
        while (att != null) {
            if (att.attribute.equals(name))
                break;
            pre = att;
            att = att.next;
        }
        if (att != null) {
            if (pre == null) {
                this.adaptee.attributes = att.next;
            } else {
                pre.next = att.next;
            }
        }
    }

    /**
     * @see org.w3c.dom.Element#getAttributeNode
     */
    public org.w3c.dom.Attr getAttributeNode(String name) {
        if (this.adaptee == null)
            return null;

        AttVal att = this.adaptee.attributes;
        while (att != null) {
            if (att.attribute.equals(name))
                break;
            att = att.next;
        }
        if (att != null)
            return att.getAdapter();
        else
            return null;
    }

    /**
     * @see org.w3c.dom.Element#setAttributeNode
     */
    public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr newAttr) throws DOMException {
        if (newAttr == null)
            return null;
        if (!(newAttr instanceof DOMAttrImpl)) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR, "newAttr not instanceof DOMAttrImpl");
        }

        DOMAttrImpl newatt = (DOMAttrImpl) newAttr;
        String name = newatt.avAdaptee.attribute;
        org.w3c.dom.Attr result = null;

        AttVal att = this.adaptee.attributes;
        while (att != null) {
            if (att.attribute.equals(name))
                break;
            att = att.next;
        }
        if (att != null) {
            result = att.getAdapter();
            att.adapter = newAttr;
        } else {
            if (this.adaptee.attributes == null) {
                this.adaptee.attributes = newatt.avAdaptee;
            } else {
                newatt.avAdaptee.next = this.adaptee.attributes;
                this.adaptee.attributes = newatt.avAdaptee;
            }
        }
        return result;
    }

    /**
     * @see org.w3c.dom.Element#removeAttributeNode
     */
    public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr oldAttr) throws DOMException {
        if (oldAttr == null)
            return null;

        org.w3c.dom.Attr result = null;
        AttVal att = this.adaptee.attributes;
        AttVal pre = null;
        while (att != null) {
            if (att.getAdapter() == oldAttr)
                break;
            pre = att;
            att = att.next;
        }
        if (att != null) {
            if (pre == null) {
                this.adaptee.attributes = att.next;
            } else {
                pre.next = att.next;
            }
            result = oldAttr;
        } else {
            throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR, "oldAttr not found");
        }
        return result;
    }

    /**
     * @see org.w3c.dom.Element#getElementsByTagName
     */
    public org.w3c.dom.NodeList getElementsByTagName(String name) {
        return new DOMNodeListByTagNameImpl(this.adaptee, name);
    }

    /**
     * @see org.w3c.dom.Element#normalize
     */
    public void normalize() {
        // NOT SUPPORTED
    }

    /**
     * DOM2 - not implemented.
     */
    public String getAttributeNS(String namespaceURI, String localName) {
        return null;
    }

    /**
     * DOM2 - not implemented.
     * 
     * @exception org.w3c.dom.DOMException
     */
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws org.w3c.dom.DOMException {
    }

    /**
     * DOM2 - not implemented.
     * 
     * @exception org.w3c.dom.DOMException
     */
    public void removeAttributeNS(String namespaceURI, String localName) throws org.w3c.dom.DOMException {
    }

    /**
     * DOM2 - not implemented.
     */
    public org.w3c.dom.Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return null;
    }

    /**
     * DOM2 - not implemented.
     * 
     * @exception org.w3c.dom.DOMException
     */
    public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr newAttr) throws org.w3c.dom.DOMException {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public org.w3c.dom.NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public boolean hasAttribute(String name) {
        return false;
    }

    /**
     * DOM2 - not implemented.
     */
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return false;
    }

    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
    }

    public short compareDocumentPosition(org.w3c.dom.Node other) throws DOMException {
        return 0;
    }

    public String getBaseURI() {
        return null;
    }

    public Object getFeature(String feature, String version) {
        return null;
    }

    public String getTextContent() throws DOMException {
        return null;
    }

    public Object getUserData(String key) {
        return null;
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        return false;
    }

    public boolean isEqualNode(org.w3c.dom.Node arg) {
        return false;
    }

    public boolean isSameNode(org.w3c.dom.Node other) {
        return false;
    }

    public String lookupNamespaceURI(String prefix) {
        return null;
    }

    public String lookupPrefix(String namespaceURI) {
        return null;
    }

    public void setTextContent(String textContent) throws DOMException {
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return null;
    }

}
