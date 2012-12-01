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
 * @(#)StreamIn.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 *
 * Input Stream
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
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

import java.io.InputStream;

public abstract class StreamIn {

    public static final int EndOfStream = -1; // EOF

    /*
     * states for ISO 2022 A document in ISO-2022 based encoding uses some ESC
     * sequences called "designator" to switch character sets. The designators
     * defined and used in ISO-2022-JP are: "ESC" + "(" + ? for ISO646 variants
     * "ESC" + "$" + ? and "ESC" + "$" + "(" + ? for multibyte character sets
     */

    public static final int FSM_ASCII    = 0;
    public static final int FSM_ESC      = 1;
    public static final int FSM_ESCD     = 2;
    public static final int FSM_ESCDP    = 3;
    public static final int FSM_ESCP     = 4;
    public static final int FSM_NONASCII = 5;

    /* non-raw input is cleaned up */
    public int         state; /* FSM for ISO2022 */
    public boolean     pushed;
    public int         c;
    public int         tabs;
    public int         tabsize;
    public int         lastcol;
    public int         curcol;
    public int         curline;
    public int         encoding;
    public InputStream stream;
    public boolean     endOfStream;
    public Object      lexer; /* needed for error reporting */

    /* read char from stream */
    public abstract int readCharFromStream();

    public abstract int readChar();

    public abstract void ungetChar(int c);

    public abstract boolean isEndOfStream();
}
