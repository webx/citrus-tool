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
 * @(#)DOMDocumentTypeImpl.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 * DOMDocumentTypeImpl (c) 1998-2000 (W3C) MIT, INRIA, Keio University See
 * Tidy.java for the copyright notice. Derived from <a
 * href="http://www.w3.org/People/Raggett/tidy"> HTML Tidy Release 4 Aug
 * 2000</a>
 * 
 * @author Dave Raggett <dsr@w3.org>
 * @author Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.7, 1999/12/06 Tidy Release 30 Nov 1999
 * @version 1.8, 2000/01/22 Tidy Release 13 Jan 2000
 * @version 1.9, 2000/06/03 Tidy Release 30 Apr 2000
 * @version 1.10, 2000/07/22 Tidy Release 8 Jul 2000
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class DOMDocumentTypeImpl extends DOMNodeImpl implements org.w3c.dom.DocumentType {

    protected DOMDocumentTypeImpl(Node adaptee) {
        super(adaptee);
    }

    /* --------------------- DOM ---------------------------- */

    /**
     * @see org.w3c.dom.Node#getNodeType
     */
    @Override
    public short getNodeType() {
        return org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
    }

    /**
     * @see org.w3c.dom.Node#getNodeName
     */
    @Override
    public String getNodeName() {
        return getName();
    }

    /**
     * @see org.w3c.dom.DocumentType#getName
     */
    public String getName() {
        String value = null;
        if (adaptee.type == Node.DocTypeTag) {

            if (adaptee.textarray != null && adaptee.start < adaptee.end) {
                value = Lexer.getString(adaptee.textarray, adaptee.start, adaptee.end - adaptee.start);
            }
        }
        return value;
    }

    public org.w3c.dom.NamedNodeMap getEntities() {
        // NOT SUPPORTED
        return null;
    }

    public org.w3c.dom.NamedNodeMap getNotations() {
        // NOT SUPPORTED
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public String getPublicId() {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public String getSystemId() {
        return null;
    }

    /**
     * DOM2 - not implemented.
     */
    public String getInternalSubset() {
        return null;
    }

}
