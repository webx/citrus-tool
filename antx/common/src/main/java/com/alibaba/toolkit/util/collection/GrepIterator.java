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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 根据指定的过滤条件<code>Predicate</code>, 过滤指定的<code>Iterator</code>.
 * 
 * @version $Id: GrepIterator.java,v 1.1 2003/07/03 07:26:16 baobao Exp $
 * @author Michael Zhou
 */
public class GrepIterator extends FilterIterator {
    private Predicate predicate;
    private Object nextObject;
    private boolean nextObjectSet = false;

    /**
     * 创建一个<code>GrepIterator</code>.
     * 
     * @param iterator 被过滤的<code>Iterator</code>
     * @param predicate 过滤条件
     */
    public GrepIterator(Iterator iterator, Predicate predicate) {
        super(iterator);
        this.predicate = predicate;
    }

    /**
     * 取得"断言"对象.
     * 
     * @return "断言"对象
     */
    public Predicate getPredicate() {
        return predicate;
    }

    /**
     * 判断是否有下一个元素.
     * 
     * @return 如果有下一个元素, 则返回<code>true</code>
     */
    @Override
    public boolean hasNext() {
        if (nextObjectSet) {
            return true;
        } else {
            return setNextObject();
        }
    }

    /**
     * 取得下一个元素.
     * 
     * @return 一下个符合条件的元素
     */
    @Override
    public Object next() {
        if (!nextObjectSet && !setNextObject()) {
            throw new NoSuchElementException();
        }

        nextObjectSet = false;
        return nextObject;
    }

    /**
     * 删除最进返回的元素, 不支持.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * 设置下一个可用的元素.
     * 
     * @return 如果没有下一个元素了, 则返回<code>false</code>, 否则返回<code>true</code>
     */
    private boolean setNextObject() {
        Iterator iterator = getIterator();
        Predicate predicate = getPredicate();

        while (iterator.hasNext()) {
            Object object = iterator.next();

            if (predicate.evaluate(object)) {
                nextObject = object;
                nextObjectSet = true;
                return true;
            }
        }

        return false;
    }
}
