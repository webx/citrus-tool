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
 * @(#)Dict.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 * Tag dictionary node (c) 1998-2000 (W3C) MIT, INRIA, Keio University See
 * Tidy.java for the copyright notice. Derived from <a
 * href="http://www.w3.org/People/Raggett/tidy"> HTML Tidy Release 4 Aug
 * 2000</a>
 * 
 * @author Dave Raggett <dsr@w3.org>
 * @author Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.0, 1999/05/22
 * @version 1.0.1, 1999/05/29
 * @version 1.1, 1999/06/18 Java Bean
 * @version 1.2, 1999/07/10 Tidy Release 7 Jul 1999
 * @version 1.3, 1999/07/30 Tidy Release 26 Jul 1999
 * @version 1.4, 1999/09/04 DOM support
 * @version 1.5, 1999/10/23 Tidy Release 27 Sep 1999
 * @version 1.6, 1999/11/01 Tidy Release 22 Oct 1999
 * @version 1.7, 1999/12/06 Tidy Release 30 Nov 1999
 * @version 1.8, 2000/01/22 Tidy Release 13 Jan 2000
 * @version 1.9, 2000/06/03 Tidy Release 30 Apr 2000
 * @version 1.10, 2000/07/22 Tidy Release 8 Jul 2000
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class Dict {

    /* content model shortcut encoding */

    public static final int CM_UNKNOWN = 0;
    public static final int CM_EMPTY = 1 << 0;
    public static final int CM_HTML = 1 << 1;
    public static final int CM_HEAD = 1 << 2;
    public static final int CM_BLOCK = 1 << 3;
    public static final int CM_INLINE = 1 << 4;
    public static final int CM_LIST = 1 << 5;
    public static final int CM_DEFLIST = 1 << 6;
    public static final int CM_TABLE = 1 << 7;
    public static final int CM_ROWGRP = 1 << 8;
    public static final int CM_ROW = 1 << 9;
    public static final int CM_FIELD = 1 << 10;
    public static final int CM_OBJECT = 1 << 11;
    public static final int CM_PARAM = 1 << 12;
    public static final int CM_FRAMES = 1 << 13;
    public static final int CM_HEADING = 1 << 14;
    public static final int CM_OPT = 1 << 15;
    public static final int CM_IMG = 1 << 16;
    public static final int CM_MIXED = 1 << 17;
    public static final int CM_NO_INDENT = 1 << 18;
    public static final int CM_OBSOLETE = 1 << 19;
    public static final int CM_NEW = 1 << 20;
    public static final int CM_OMITST = 1 << 21;

    /*
     * If the document uses just HTML 2.0 tags and attributes described it as
     * HTML 2.0 Similarly for HTML 3.2 and the 3 flavors of HTML 4.0. If there
     * are proprietary tags and attributes then describe it as HTML Proprietary.
     * If it includes the xml-lang or xmlns attributes but is otherwise HTML
     * 2.0, 3.2 or 4.0 then describe it as one of the flavors of Voyager
     * (strict, loose or frameset).
     */

    public static final short VERS_UNKNOWN = 0;

    public static final short VERS_HTML20 = 1;
    public static final short VERS_HTML32 = 2;
    public static final short VERS_HTML40_STRICT = 4;
    public static final short VERS_HTML40_LOOSE = 8;
    public static final short VERS_FRAMES = 16;
    public static final short VERS_XML = 32;

    public static final short VERS_NETSCAPE = 64;
    public static final short VERS_MICROSOFT = 128;
    public static final short VERS_SUN = 256;

    public static final short VERS_MALFORMED = 512;

    public static final short VERS_ALL = VERS_HTML20 | VERS_HTML32 | VERS_HTML40_STRICT | VERS_HTML40_LOOSE
            | VERS_FRAMES;
    public static final short VERS_HTML40 = VERS_HTML40_STRICT | VERS_HTML40_LOOSE | VERS_FRAMES;
    public static final short VERS_LOOSE = VERS_HTML32 | VERS_HTML40_LOOSE | VERS_FRAMES;
    public static final short VERS_IFRAMES = VERS_HTML40_LOOSE | VERS_FRAMES;
    public static final short VERS_FROM32 = VERS_HTML40_STRICT | VERS_LOOSE;
    public static final short VERS_PROPRIETARY = VERS_NETSCAPE | VERS_MICROSOFT | VERS_SUN;

    public static final short VERS_EVERYTHING = VERS_ALL | VERS_PROPRIETARY;

    public Dict(String name, short versions, int model, Parser parser, CheckAttribs chkattrs) {
        this.name = name;
        this.versions = versions;
        this.model = model;
        this.parser = parser;
        this.chkattrs = chkattrs;
    }

    public String name;
    public short versions;
    public int model;
    public Parser parser;
    public CheckAttribs chkattrs;
}
