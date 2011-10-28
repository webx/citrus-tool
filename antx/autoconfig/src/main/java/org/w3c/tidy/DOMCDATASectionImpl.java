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
 * @(#)DOMCDATASectionImpl.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 * DOMCDATASectionImpl (c) 1998-2000 (W3C) MIT, INRIA, Keio University See
 * Tidy.java for the copyright notice. Derived from <a
 * href="http://www.w3.org/People/Raggett/tidy"> HTML Tidy Release 4 Aug
 * 2000</a>
 * 
 * @author Dave Raggett <dsr@w3.org>
 * @author Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @author Gary L Peskin <garyp@firstech.com>
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class DOMCDATASectionImpl extends DOMTextImpl implements org.w3c.dom.CDATASection {

    protected DOMCDATASectionImpl(Node adaptee) {
        super(adaptee);
    }

    /* --------------------- DOM ---------------------------- */

    /**
     * @see org.w3c.dom.Node#getNodeName
     */
    @Override
    public String getNodeName() {
        return "#cdata-section";
    }

    /**
     * @see org.w3c.dom.Node#getNodeType
     */
    @Override
    public short getNodeType() {
        return org.w3c.dom.Node.CDATA_SECTION_NODE;
    }
}
