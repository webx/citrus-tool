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
 * @(#)OutImpl.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 *
 * Output Stream Implementation
 *
 * (c) 1998-2000 (W3C) MIT, INRIA, Keio University
 * See Tidy.java for the copyright notice.
 * Derived from <a href="http://www.w3.org/People/Raggett/tidy">
 * HTML Tidy Release 4 Aug 2000</a>
 *
 * @author  Dave Raggett <dsr@w3.org>
 * @author  Andy Quick <ac.quick@sympatico.ca> (translation to Java)
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

import java.io.IOException;

public class OutImpl extends Out {

    public OutImpl() {
        this.out = null;
    }

    public void outc(byte c) {
        outc(((int) c) & 0xFF); // Convert to unsigned.
    }

    /* For mac users, should we map Unicode back to MacRoman? */
    public void outc(int c) {
        int ch;

        try {
            if (this.encoding == Configuration.UTF8) {
                if (c < 128)
                    this.out.write(c);
                else if (c <= 0x7FF) {
                    ch = (0xC0 | (c >> 6));
                    this.out.write(ch);
                    ch = (0x80 | (c & 0x3F));
                    this.out.write(ch);
                } else if (c <= 0xFFFF) {
                    ch = (0xE0 | (c >> 12));
                    this.out.write(ch);
                    ch = (0x80 | ((c >> 6) & 0x3F));
                    this.out.write(ch);
                    ch = (0x80 | (c & 0x3F));
                    this.out.write(ch);
                } else if (c <= 0x1FFFFF) {
                    ch = (0xF0 | (c >> 18));
                    this.out.write(ch);
                    ch = (0x80 | ((c >> 12) & 0x3F));
                    this.out.write(ch);
                    ch = (0x80 | ((c >> 6) & 0x3F));
                    this.out.write(ch);
                    ch = (0x80 | (c & 0x3F));
                    this.out.write(ch);
                } else {
                    ch = (0xF8 | (c >> 24));
                    this.out.write(ch);
                    ch = (0x80 | ((c >> 18) & 0x3F));
                    this.out.write(ch);
                    ch = (0x80 | ((c >> 12) & 0x3F));
                    this.out.write(ch);
                    ch = (0x80 | ((c >> 6) & 0x3F));
                    this.out.write(ch);
                    ch = (0x80 | (c & 0x3F));
                    this.out.write(ch);
                }
            } else if (this.encoding == Configuration.ISO2022) {
                if (c == 0x1b) /* ESC */
                    this.state = StreamIn.FSM_ESC;
                else {
                    switch (this.state) {
                        case StreamIn.FSM_ESC:
                            if (c == '$')
                                this.state = StreamIn.FSM_ESCD;
                            else if (c == '(')
                                this.state = StreamIn.FSM_ESCP;
                            else
                                this.state = StreamIn.FSM_ASCII;
                            break;

                        case StreamIn.FSM_ESCD:
                            if (c == '(')
                                this.state = StreamIn.FSM_ESCDP;
                            else
                                this.state = StreamIn.FSM_NONASCII;
                            break;

                        case StreamIn.FSM_ESCDP:
                            this.state = StreamIn.FSM_NONASCII;
                            break;

                        case StreamIn.FSM_ESCP:
                            this.state = StreamIn.FSM_ASCII;
                            break;

                        case StreamIn.FSM_NONASCII:
                            c &= 0x7F;
                            break;
                    }
                }

                this.out.write(c);
            } else
                this.out.write(c);
        } catch (IOException e) {
            System.err.println("OutImpl.outc: " + e.toString());
        }
    }

    public void newline() {
        try {
            this.out.write(nlBytes);
            this.out.flush();
        } catch (IOException e) {
            System.err.println("OutImpl.newline: " + e.toString());
        }
    }

    private static final byte[] nlBytes = (System.getProperty("line.separator")).getBytes();

};
