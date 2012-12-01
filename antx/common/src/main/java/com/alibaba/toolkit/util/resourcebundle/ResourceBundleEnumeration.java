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

package com.alibaba.toolkit.util.resourcebundle;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 将一个集合和一个<code>Enumeration</code>结合的<code>Enumeration</code>, 用来遍历resource
 * bundle及其父bundle中的所有内容.
 *
 * @author Michael Zhou
 * @version $Id: ResourceBundleEnumeration.java,v 1.1 2003/07/03 07:26:35 baobao
 *          Exp $
 */
public class ResourceBundleEnumeration implements Enumeration {
    private Set         set;
    private Iterator    iterator;
    private Enumeration enumeration; // 可以为null
    private Object next = null;

    /**
     * 创建一个<code>Enumeration</code>.
     *
     * @param set         集合
     * @param enumeration <code>Enumeration</code>对象, 可以为<code>null</code>
     */
    public ResourceBundleEnumeration(Set set, Enumeration enumeration) {
        this.set = set;
        this.iterator = set.iterator();
        this.enumeration = enumeration;
    }

    /**
     * 判断是否有下一个元素.
     *
     * @return 如果还有下一个元素, 则返回<code>true</code>
     */
    public boolean hasMoreElements() {
        if (next == null) {
            if (iterator.hasNext()) {
                next = iterator.next();
            } else if (enumeration != null) {
                while (next == null && enumeration.hasMoreElements()) {
                    next = enumeration.nextElement();

                    if (set.contains(next)) {
                        next = null;
                    }
                }
            }
        }

        return next != null;
    }

    /**
     * 取得下一个元素.
     *
     * @return 下一个元素的值
     */
    public Object nextElement() {
        if (hasMoreElements()) {
            Object result = next;

            next = null;
            return result;
        } else {
            throw new NoSuchElementException();
        }
    }
}
