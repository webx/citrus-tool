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
 *
 */
package com.alibaba.toolkit.util.collection;

import java.util.Iterator;

/**
 * Iterator的过滤器.
 *
 * @version $Id: FilterIterator.java,v 1.1 2003/07/03 07:26:16 baobao Exp $
 * @author Michael Zhou
 */
public abstract class FilterIterator implements Iterator {
    private Iterator iterator;

    /**
     * 创建一个过滤器.
     *
     * @param iterator  被过滤的<code>Iterator</code>
     */
    public FilterIterator(Iterator iterator) {
        this.iterator = iterator;
    }

    /**
     * 取得被过滤的<code>Iterator</code>.
     *
     * @return 被过滤的<code>Iterator</code>
     */
    public Iterator getIterator() {
        return iterator;
    }

    /**
     * 是否存在下一个元素.
     *
     * @return 如果存在下一个元素, 则返回<code>true</code>
     */
    public boolean hasNext() {
        return getIterator().hasNext();
    }

    /**
     * 取得下一个元素.
     *
     * @return 下一个元素
     */
    public Object next() {
        return getIterator().next();
    }

    /**
     * 删除最近返回的元素.
     */
    public void remove() {
        getIterator().remove();
    }
}
