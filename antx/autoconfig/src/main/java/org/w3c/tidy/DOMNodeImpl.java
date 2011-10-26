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
 * @(#)DOMNodeImpl.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

import org.w3c.dom.DOMException;
import org.w3c.dom.UserDataHandler;

/**
 * DOMNodeImpl (c) 1998-2000 (W3C) MIT, INRIA, Keio University See Tidy.java for
 * the copyright notice. Derived from <a
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

public class DOMNodeImpl implements org.w3c.dom.Node {

    protected Node adaptee;

    protected DOMNodeImpl(Node adaptee) {
        this.adaptee = adaptee;
    }

    /* --------------------- DOM ---------------------------- */

    /**
     * @see org.w3c.dom.Node#getNodeValue
     */
    public String getNodeValue() throws DOMException {
        String value = ""; //BAK 10/10/2000 replaced null
        if (adaptee.type == Node.TextNode || adaptee.type == Node.CDATATag || adaptee.type == Node.CommentTag
                || adaptee.type == Node.ProcInsTag) {

            if (adaptee.textarray != null && adaptee.start < adaptee.end) {
                value = Lexer.getString(adaptee.textarray, adaptee.start, adaptee.end - adaptee.start);
            }
        }
        return value;
    }

    /**
     * @see org.w3c.dom.Node#setNodeValue
     */
    public void setNodeValue(String nodeValue) throws DOMException {
        if (adaptee.type == Node.TextNode || adaptee.type == Node.CDATATag || adaptee.type == Node.CommentTag
                || adaptee.type == Node.ProcInsTag) {
            byte[] textarray = Lexer.getBytes(nodeValue);
            adaptee.textarray = textarray;
            adaptee.start = 0;
            adaptee.end = textarray.length;
        }
    }

    /**
     * @see org.w3c.dom.Node#getNodeName
     */
    public String getNodeName() {
        return adaptee.element;
    }

    /**
     * @see org.w3c.dom.Node#getNodeType
     */
    public short getNodeType() {
        short result = -1;
        switch (adaptee.type) {
            case Node.RootNode:
                result = org.w3c.dom.Node.DOCUMENT_NODE;
                break;
            case Node.DocTypeTag:
                result = org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
                break;
            case Node.CommentTag:
                result = org.w3c.dom.Node.COMMENT_NODE;
                break;
            case Node.ProcInsTag:
                result = org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE;
                break;
            case Node.TextNode:
                result = org.w3c.dom.Node.TEXT_NODE;
                break;
            case Node.CDATATag:
                result = org.w3c.dom.Node.CDATA_SECTION_NODE;
                break;
            case Node.StartTag:
            case Node.StartEndTag:
                result = org.w3c.dom.Node.ELEMENT_NODE;
                break;
        }
        return result;
    }

    /**
     * @see org.w3c.dom.Node#getParentNode
     */
    public org.w3c.dom.Node getParentNode() {
        if (adaptee.parent != null)
            return adaptee.parent.getAdapter();
        else
            return null;
    }

    /**
     * @see org.w3c.dom.Node#getChildNodes
     */
    public org.w3c.dom.NodeList getChildNodes() {
        return new DOMNodeListImpl(adaptee);
    }

    /**
     * @see org.w3c.dom.Node#getFirstChild
     */
    public org.w3c.dom.Node getFirstChild() {
        if (adaptee.content != null)
            return adaptee.content.getAdapter();
        else
            return null;
    }

    /**
     * @see org.w3c.dom.Node#getLastChild
     */
    public org.w3c.dom.Node getLastChild() {
        if (adaptee.last != null)
            return adaptee.last.getAdapter();
        else
            return null;
    }

    /**
     * @see org.w3c.dom.Node#getPreviousSibling
     */
    public org.w3c.dom.Node getPreviousSibling() {
        if (adaptee.prev != null)
            return adaptee.prev.getAdapter();
        else
            return null;
    }

    /**
     * @see org.w3c.dom.Node#getNextSibling
     */
    public org.w3c.dom.Node getNextSibling() {
        if (adaptee.next != null)
            return adaptee.next.getAdapter();
        else
            return null;
    }

    /**
     * @see org.w3c.dom.Node#getAttributes
     */
    public org.w3c.dom.NamedNodeMap getAttributes() {
        return new DOMAttrMapImpl(adaptee.attributes);
    }

    /**
     * @see org.w3c.dom.Node#getOwnerDocument
     */
    public org.w3c.dom.Document getOwnerDocument() {
        Node node;

        node = this.adaptee;
        if (node != null && node.type == Node.RootNode)
            return null;

        for (node = this.adaptee; node != null && node.type != Node.RootNode; node = node.parent)
            ;

        if (node != null)
            return (org.w3c.dom.Document) node.getAdapter();
        else
            return null;
    }

    /**
     * @see org.w3c.dom.Node#insertBefore
     */
    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node newChild, org.w3c.dom.Node refChild) throws DOMException {
        // TODO - handle newChild already in tree

        if (newChild == null)
            return null;
        if (!(newChild instanceof DOMNodeImpl)) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR, "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl) newChild;

        if (this.adaptee.type == Node.RootNode) {
            if (newCh.adaptee.type != Node.DocTypeTag && newCh.adaptee.type != Node.ProcInsTag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        } else if (this.adaptee.type == Node.StartTag) {
            if (newCh.adaptee.type != Node.StartTag && newCh.adaptee.type != Node.StartEndTag
                    && newCh.adaptee.type != Node.CommentTag && newCh.adaptee.type != Node.TextNode
                    && newCh.adaptee.type != Node.CDATATag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        if (refChild == null) {
            Node.insertNodeAtEnd(this.adaptee, newCh.adaptee);
            if (this.adaptee.type == Node.StartEndTag) {
                this.adaptee.setType(Node.StartTag);
            }
        } else {
            Node ref = this.adaptee.content;
            while (ref != null) {
                if (ref.getAdapter() == refChild)
                    break;
                ref = ref.next;
            }
            if (ref == null) {
                throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR, "refChild not found");
            }
            Node.insertNodeBeforeElement(ref, newCh.adaptee);
        }
        return newChild;
    }

    /**
     * @see org.w3c.dom.Node#replaceChild
     */
    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node newChild, org.w3c.dom.Node oldChild) throws DOMException {
        // TODO - handle newChild already in tree

        if (newChild == null)
            return null;
        if (!(newChild instanceof DOMNodeImpl)) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR, "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl) newChild;

        if (this.adaptee.type == Node.RootNode) {
            if (newCh.adaptee.type != Node.DocTypeTag && newCh.adaptee.type != Node.ProcInsTag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        } else if (this.adaptee.type == Node.StartTag) {
            if (newCh.adaptee.type != Node.StartTag && newCh.adaptee.type != Node.StartEndTag
                    && newCh.adaptee.type != Node.CommentTag && newCh.adaptee.type != Node.TextNode
                    && newCh.adaptee.type != Node.CDATATag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        if (oldChild == null) {
            throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR, "oldChild not found");
        } else {
            Node n;
            Node ref = this.adaptee.content;
            while (ref != null) {
                if (ref.getAdapter() == oldChild)
                    break;
                ref = ref.next;
            }
            if (ref == null) {
                throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR, "oldChild not found");
            }
            newCh.adaptee.next = ref.next;
            newCh.adaptee.prev = ref.prev;
            newCh.adaptee.last = ref.last;
            newCh.adaptee.parent = ref.parent;
            newCh.adaptee.content = ref.content;
            if (ref.parent != null) {
                if (ref.parent.content == ref)
                    ref.parent.content = newCh.adaptee;
                if (ref.parent.last == ref)
                    ref.parent.last = newCh.adaptee;
            }
            if (ref.prev != null) {
                ref.prev.next = newCh.adaptee;
            }
            if (ref.next != null) {
                ref.next.prev = newCh.adaptee;
            }
            for (n = ref.content; n != null; n = n.next) {
                if (n.parent == ref)
                    n.parent = newCh.adaptee;
            }
        }
        return oldChild;
    }

    /**
     * @see org.w3c.dom.Node#removeChild
     */
    public org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild) throws DOMException {
        if (oldChild == null)
            return null;

        Node ref = this.adaptee.content;
        while (ref != null) {
            if (ref.getAdapter() == oldChild)
                break;
            ref = ref.next;
        }
        if (ref == null) {
            throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR, "refChild not found");
        }
        Node.discardElement(ref);

        if (this.adaptee.content == null && this.adaptee.type == Node.StartTag) {
            this.adaptee.setType(Node.StartEndTag);
        }

        return oldChild;
    }

    /**
     * @see org.w3c.dom.Node#appendChild
     */
    public org.w3c.dom.Node appendChild(org.w3c.dom.Node newChild) throws DOMException {
        // TODO - handle newChild already in tree

        if (newChild == null)
            return null;
        if (!(newChild instanceof DOMNodeImpl)) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR, "newChild not instanceof DOMNodeImpl");
        }
        DOMNodeImpl newCh = (DOMNodeImpl) newChild;

        if (this.adaptee.type == Node.RootNode) {
            if (newCh.adaptee.type != Node.DocTypeTag && newCh.adaptee.type != Node.ProcInsTag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        } else if (this.adaptee.type == Node.StartTag) {
            if (newCh.adaptee.type != Node.StartTag && newCh.adaptee.type != Node.StartEndTag
                    && newCh.adaptee.type != Node.CommentTag && newCh.adaptee.type != Node.TextNode
                    && newCh.adaptee.type != Node.CDATATag) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR,
                        "newChild cannot be a child of this node");
            }
        }
        Node.insertNodeAtEnd(this.adaptee, newCh.adaptee);

        if (this.adaptee.type == Node.StartEndTag) {
            this.adaptee.setType(Node.StartTag);
        }

        return newChild;
    }

    /**
     * @see org.w3c.dom.Node#hasChildNodes
     */
    public boolean hasChildNodes() {
        return (adaptee.content != null);
    }

    /**
     * @see org.w3c.dom.Node#cloneNode
     */
    public org.w3c.dom.Node cloneNode(boolean deep) {
        Node node = adaptee.cloneNode(deep);
        node.parent = null;
        return node.getAdapter();
    }

    /**
     * DOM2 - not implemented.
     */
    public void normalize() {
    }

    /**
     * DOM2 - not implemented.
     */
    public boolean supports(String feature, String version) {
        return isSupported(feature, version);
    }

    /**
     * DOM2 - not implemented.
     */
    public String getNamespaceURI() {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public String getPrefix() {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public void setPrefix(String prefix) throws DOMException {
    }

    /**
     * DOM2 - not implemented.
     */
    public String getLocalName() {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public boolean isSupported(String feature, String version) {
        return false;
    }

    /**
     * DOM2 - @see org.w3c.dom.Node#hasAttributes contributed by
     * dlp@users.sourceforge.net
     */
    public boolean hasAttributes() {
        return adaptee.attributes != null;
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
