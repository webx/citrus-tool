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

package com.alibaba.toolkit.util.collection;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * 将<code>Enumeration</code>转换成<code>Iterator</code>的适配器.
 *
 * @author Michael Zhou
 * @version $Id: EnumerationIterator.java,v 1.1 2003/07/03 07:26:16 baobao Exp $
 */
public class EnumerationIterator implements Iterator {
    private Enumeration enumeration;
    private Object      lastReturned;

    /**
     * 创建一个<code>EnumerationIterator</code>.
     *
     * @param enumeration 被适配的<code>Enumeration</code>
     */
    public EnumerationIterator(Enumeration enumeration) {
        this.enumeration = enumeration;
    }

    /**
     * 取得被适配的<code>Enumeration</code>.
     *
     * @return 被适配的<code>Enumeration</code>
     */
    public Enumeration getEnumeration() {
        return enumeration;
    }

    /**
     * 是否有下一个元素.
     *
     * @return 如果有下一个元素, 则返回<code>true</code>
     */
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }

    /**
     * 取得下一个元素.
     *
     * @return 下一个元素
     */
    public Object next() {
        return lastReturned = enumeration.nextElement();
    }

    /** 删除最近返回的元素, 不支持. */
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }
}
