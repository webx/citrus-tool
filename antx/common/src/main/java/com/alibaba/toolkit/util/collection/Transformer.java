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

/**
 * 将一个对象转换成另一个对象的接口.
 *
 * @author Michael Zhou
 * @version $Id: Transformer.java,v 1.1 2003/07/03 07:26:16 baobao Exp $
 */
public interface Transformer {
    /**
     * 将对象转换成另一个对象.
     *
     * @param input 被转换的对象
     * @return 转换后的对象
     */
    public Object transform(Object input);
}
