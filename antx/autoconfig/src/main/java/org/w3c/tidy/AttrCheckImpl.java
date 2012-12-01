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
 * @(#)AttrCheckImpl.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 * Check attribute values implementations (c) 1998-2000 (W3C) MIT, INRIA, Keio
 * University See Tidy.java for the copyright notice. Derived from <a
 * href="http://www.w3.org/People/Raggett/tidy"> HTML Tidy Release 4 Aug
 * 2000</a>
 *
 * @author Dave Raggett <dsr@w3.org>
 * @author Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class AttrCheckImpl {

    public static class CheckUrl implements AttrCheck {

        public void check(Lexer lexer, Node node, AttVal attval) {
            if (attval.value == null) {
                Report.attrError(lexer, node, attval.attribute, Report.MISSING_ATTR_VALUE);
            } else if (lexer.configuration.FixBackslash) {
                attval.value = attval.value.replace('\\', '/');
            }
        }
    }

    ;

    public static class CheckScript implements AttrCheck {

        public void check(Lexer lexer, Node node, AttVal attval) {
        }
    }

    ;

    public static class CheckAlign implements AttrCheck {

        public void check(Lexer lexer, Node node, AttVal attval) {
            String value;

            /* IMG, OBJECT, APPLET and EMBED use align for vertical position */
            if (node.tag != null && (node.tag.model & Dict.CM_IMG) != 0) {
                getCheckValign().check(lexer, node, attval);
                return;
            }

            value = attval.value;

            if (value == null) {
                Report.attrError(lexer, node, attval.attribute, Report.MISSING_ATTR_VALUE);
            } else if (!(Lexer.wstrcasecmp(value, "left") == 0 || Lexer.wstrcasecmp(value, "center") == 0
                         || Lexer.wstrcasecmp(value, "right") == 0 || Lexer.wstrcasecmp(value, "justify") == 0)) {
                Report.attrError(lexer, node, attval.value, Report.BAD_ATTRIBUTE_VALUE);
            }
        }
    }

    ;

    public static class CheckValign implements AttrCheck {

        public void check(Lexer lexer, Node node, AttVal attval) {
            String value;

            value = attval.value;

            if (value == null) {
                Report.attrError(lexer, node, attval.attribute, Report.MISSING_ATTR_VALUE);
            } else if (Lexer.wstrcasecmp(value, "top") == 0 || Lexer.wstrcasecmp(value, "middle") == 0
                       || Lexer.wstrcasecmp(value, "bottom") == 0 || Lexer.wstrcasecmp(value, "baseline") == 0) {
                /* all is fine */
            } else if (Lexer.wstrcasecmp(value, "left") == 0 || Lexer.wstrcasecmp(value, "right") == 0) {
                if (!(node.tag != null && (node.tag.model & Dict.CM_IMG) != 0)) {
                    Report.attrError(lexer, node, value, Report.BAD_ATTRIBUTE_VALUE);
                }
            } else if (Lexer.wstrcasecmp(value, "texttop") == 0 || Lexer.wstrcasecmp(value, "absmiddle") == 0
                       || Lexer.wstrcasecmp(value, "absbottom") == 0 || Lexer.wstrcasecmp(value, "textbottom") == 0) {
                lexer.versions &= Dict.VERS_PROPRIETARY;
                Report.attrError(lexer, node, value, Report.PROPRIETARY_ATTR_VALUE);
            } else {
                Report.attrError(lexer, node, value, Report.BAD_ATTRIBUTE_VALUE);
            }
        }
    }

    ;

    public static class CheckBool implements AttrCheck {

        public void check(Lexer lexer, Node node, AttVal attval) {
        }
    }

    ;

    public static class CheckId implements AttrCheck {

        public void check(Lexer lexer, Node node, AttVal attval) {
        }
    }

    ;

    public static class CheckName implements AttrCheck {

        public void check(Lexer lexer, Node node, AttVal attval) {
        }
    }

    ;

    public static AttrCheck getCheckUrl() {
        return _checkUrl;
    }

    public static AttrCheck getCheckScript() {
        return _checkScript;
    }

    public static AttrCheck getCheckAlign() {
        return _checkAlign;
    }

    public static AttrCheck getCheckValign() {
        return _checkValign;
    }

    public static AttrCheck getCheckBool() {
        return _checkBool;
    }

    public static AttrCheck getCheckId() {
        return _checkId;
    }

    public static AttrCheck getCheckName() {
        return _checkName;
    }

    private static AttrCheck _checkUrl    = new CheckUrl();
    private static AttrCheck _checkScript = new CheckScript();
    private static AttrCheck _checkAlign  = new CheckAlign();
    private static AttrCheck _checkValign = new CheckValign();
    private static AttrCheck _checkBool   = new CheckBool();
    private static AttrCheck _checkId     = new CheckId();
    private static AttrCheck _checkName   = new CheckName();
}
