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

/**
 * 将一个<code>Iterator</code>中的值转换成另一个值的过滤器.
 * 
 * @version $Id: TransformIterator.java,v 1.1 2003/07/03 07:26:16 baobao Exp $
 * @author Michael Zhou
 */
public class TransformIterator extends FilterIterator {
    private Transformer transformer;

    /**
     * 创建一个过滤器.
     * 
     * @param iterator 被过滤的<code>Iterator</code>
     * @param transformer 转换器
     */
    public TransformIterator(Iterator iterator, Transformer transformer) {
        super(iterator);
        this.transformer = transformer;
    }

    /**
     * 取得转换器.
     * 
     * @return 转换器对象
     */
    public Transformer getTransformer() {
        return transformer;
    }

    /**
     * 取得下一个对象.
     * 
     * @return 下一个经过转换的对象
     */
    @Override
    public Object next() {
        return transform(super.next());
    }

    /**
     * 转换对象.
     * 
     * @param input 输入对象
     * @return 转换后的对象
     */
    private Object transform(Object input) {
        Transformer transformer = getTransformer();

        if (transformer != null) {
            return transformer.transform(input);
        }

        return input;
    }
}
