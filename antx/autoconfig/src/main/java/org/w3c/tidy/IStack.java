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
 * @(#)IStack.java   1.11 2000/08/16
 *
 */

package org.w3c.tidy;

/**
 * Inline stack node (c) 1998-2000 (W3C) MIT, INRIA, Keio University See
 * Tidy.java for the copyright notice. Derived from <a
 * href="http://www.w3.org/People/Raggett/tidy"> HTML Tidy Release 4 Aug
 * 2000</a>
 *
 * @author Dave Raggett <dsr@w3.org>
 * @author Andy Quick <ac.quick@sympatico.ca> (translation to Java)
 * @version 1.11, 2000/08/16 Tidy Release 4 Aug 2000
 */

public class IStack {

    /*
     * Mosaic handles inlines via a separate stack from other elements We
     * duplicate this to recover from inline markup errors such as: <i>italic
     * text <p>more italic text</b> normal text which for compatibility with
     * Mosaic is mapped to: <i>italic text</i> <p><i>more italic text</i> normal
     * text Note that any inline end tag pop's the effect of the current inline
     * start tag, so that </b> pop's <i> in the above example.
     */

    public IStack next;
    public Dict   tag; /* tag's dictionary definition */
    public String element; /* name (null for text nodes) */
    public AttVal attributes;

    public IStack() {
        next = null;
        tag = null;
        element = null;
        attributes = null;
    }
}
