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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 对数组实现<code>Iterator</code>接口.
 *
 * @author Michael Zhou
 * @version $Id: ArrayIterator.java,v 1.1 2003/07/03 07:26:15 baobao Exp $
 */
public class ArrayIterator implements Iterator {
    private Object array;
    private int length = 0;
    private int index  = 0;

    /**
     * 创建一个<code>ArrayIterator</code>.
     *
     * @param array 要遍历的数组
     */
    public ArrayIterator(Object array) {
        this.array = array;
        this.length = Array.getLength(array);
    }

    /**
     * 取得被遍历的数组.
     *
     * @return 被遍历的数组
     */
    public Object getArray() {
        return array;
    }

    /**
     * 是否还有下一个数组元素.
     *
     * @return 如果还有下一个数组元素, 则返回<code>true</code>
     */
    public boolean hasNext() {
        return index < length;
    }

    /**
     * 取得下一个数组元素.
     *
     * @return 下一个数组元素
     */
    public Object next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return Array.get(array, index++);
    }

    /** 删除当前元素, 不支持. */
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }
}
