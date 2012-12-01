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
 * @(#)AttVal.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 * Attribute/Value linked list node (c) 1998-2000 (W3C) MIT, INRIA, Keio
 * University See Tidy.java for the copyright notice. Derived from <a
 * href="http://www.w3.org/People/Raggett/tidy"> HTML Tidy Release 4 Aug
 * 2000</a>
 *
 * @author Dave Raggett <dsr@w3.org>
 * @author Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class AttVal extends Object implements Cloneable {

    public AttVal    next;
    public Attribute dict;
    public Node      asp;
    public Node      php;
    public int       delim;
    public String    attribute;
    public String    value;

    public AttVal() {
        this.next = null;
        this.dict = null;
        this.asp = null;
        this.php = null;
        this.delim = 0;
        this.attribute = null;
        this.value = null;
    }

    public AttVal(AttVal next, Attribute dict, int delim, String attribute, String value) {
        this.next = next;
        this.dict = dict;
        this.asp = null;
        this.php = null;
        this.delim = delim;
        this.attribute = attribute;
        this.value = value;
    }

    public AttVal(AttVal next, Attribute dict, Node asp, Node php, int delim, String attribute, String value) {
        this.next = next;
        this.dict = dict;
        this.asp = asp;
        this.php = php;
        this.delim = delim;
        this.attribute = attribute;
        this.value = value;
    }

    @Override
    protected Object clone() {
        AttVal av = new AttVal();
        if (next != null) {
            av.next = (AttVal) next.clone();
        }
        if (attribute != null) {
            av.attribute = attribute;
        }
        if (value != null) {
            av.value = value;
        }
        av.delim = delim;
        if (asp != null) {
            av.asp = (Node) asp.clone();
        }
        if (php != null) {
            av.php = (Node) php.clone();
        }
        av.dict = AttributeTable.getDefaultAttributeTable().findAttribute(this);
        return av;
    }

    public boolean isBoolAttribute() {
        Attribute attribute = this.dict;
        if (attribute != null) {
            if (attribute.attrchk == AttrCheckImpl.getCheckBool()) {
                return true;
            }
        }

        return false;
    }

    /* ignore unknown attributes for proprietary elements */
    public Attribute checkAttribute(Lexer lexer, Node node) {
        TagTable tt = lexer.configuration.tt;

        if (this.asp == null && this.php == null) {
            this.checkUniqueAttribute(lexer, node);
        }

        Attribute attribute = this.dict;
        if (attribute != null) {
            /* title is vers 2.0 for A and LINK otherwise vers 4.0 */
            if (attribute == AttributeTable.attrTitle && (node.tag == tt.tagA || node.tag == tt.tagLink)) {
                lexer.versions &= Dict.VERS_ALL;
            } else if ((attribute.versions & Dict.VERS_XML) != 0) {
                if (!(lexer.configuration.XmlTags || lexer.configuration.XmlOut)) {
                    Report.attrError(lexer, node, this.attribute, Report.XML_ATTRIBUTE_VALUE);
                }
            } else {
                lexer.versions &= attribute.versions;
            }

            if (attribute.attrchk != null) {
                attribute.attrchk.check(lexer, node, this);
            }
        } else if (!lexer.configuration.XmlTags && !(node.tag == null) && this.asp == null
                   && !(node.tag != null && (node.tag.versions & Dict.VERS_PROPRIETARY) != 0)) {
            Report.attrError(lexer, node, this.attribute, Report.UNKNOWN_ATTRIBUTE);
        }

        return attribute;
    }

    /*
     * the same attribute name can't be used more than once in each element
     */
    public void checkUniqueAttribute(Lexer lexer, Node node) {
        AttVal attr;
        int count = 0;

        for (attr = this.next; attr != null; attr = attr.next) {
            if (this.attribute != null && attr.attribute != null && attr.asp == null && attr.php == null
                && Lexer.wstrcasecmp(this.attribute, attr.attribute) == 0) {
                ++count;
            }
        }

        if (count > 0) {
            Report.attrError(lexer, node, this.attribute, Report.REPEATED_ATTRIBUTE);
        }
    }

    /* --------------------- DOM ---------------------------- */

    protected org.w3c.dom.Attr adapter = null;

    protected org.w3c.dom.Attr getAdapter() {
        if (adapter == null) {
            adapter = new DOMAttrImpl(this);
        }
        return adapter;
    }
    /* --------------------- END DOM ------------------------ */
}
