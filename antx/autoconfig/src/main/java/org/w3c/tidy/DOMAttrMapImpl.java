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
 * @(#)DOMAttrMapImpl.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

import org.w3c.dom.DOMException;

/**
 * DOMAttrMapImpl (c) 1998-2000 (W3C) MIT, INRIA, Keio University See Tidy.java
 * for the copyright notice. Derived from <a
 * href="http://www.w3.org/People/Raggett/tidy"> HTML Tidy Release 4 Aug
 * 2000</a>
 *
 * @author Dave Raggett <dsr@w3.org>
 * @author Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class DOMAttrMapImpl implements org.w3c.dom.NamedNodeMap {

    private AttVal first = null;

    protected DOMAttrMapImpl(AttVal first) {
        this.first = first;
    }

    /** @see org.w3c.dom.NamedNodeMap#getNamedItem */
    public org.w3c.dom.Node getNamedItem(String name) {
        AttVal att = this.first;
        while (att != null) {
            if (att.attribute.equals(name)) {
                break;
            }
            att = att.next;
        }
        if (att != null) {
            return att.getAdapter();
        } else {
            return null;
        }
    }

    /** @see org.w3c.dom.NamedNodeMap#setNamedItem */
    public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node arg) throws DOMException {
        // NOT SUPPORTED
        return null;
    }

    /** @see org.w3c.dom.NamedNodeMap#removeNamedItem */
    public org.w3c.dom.Node removeNamedItem(String name) throws DOMException {
        // NOT SUPPORTED
        return null;
    }

    /** @see org.w3c.dom.NamedNodeMap#item */
    public org.w3c.dom.Node item(int index) {
        int i = 0;
        AttVal att = this.first;
        while (att != null) {
            if (i >= index) {
                break;
            }
            i++;
            att = att.next;
        }
        if (att != null) {
            return att.getAdapter();
        } else {
            return null;
        }
    }

    /** @see org.w3c.dom.NamedNodeMap#getLength */
    public int getLength() {
        int len = 0;
        AttVal att = this.first;
        while (att != null) {
            len++;
            att = att.next;
        }
        return len;
    }

    /** DOM2 - not implemented. */
    public org.w3c.dom.Node getNamedItemNS(String namespaceURI, String localName) {
        return null;
    }

    /**
     * DOM2 - not implemented.
     *
     * @throws org.w3c.dom.DOMException
     */
    public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node arg) throws org.w3c.dom.DOMException {
        return null;
    }

    /**
     * DOM2 - not implemented.
     *
     * @throws org.w3c.dom.DOMException
     */
    public org.w3c.dom.Node removeNamedItemNS(String namespaceURI, String localName) throws org.w3c.dom.DOMException {
        return null;
    }
}
