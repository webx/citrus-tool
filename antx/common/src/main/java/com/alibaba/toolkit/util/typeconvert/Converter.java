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

package com.alibaba.toolkit.util.typeconvert;

/**
 * 将对象转换成另一种形式的转换器.
 *
 * @author Michael Zhou
 * @version $Id: Converter.java,v 1.1 2003/07/03 07:26:36 baobao Exp $
 */
public interface Converter {
    /**
     * 转换指定的值到指定的类型.
     *
     * @param value 要转换的值
     * @param chain 转换链, 如果一个转换器不能转换某种类型, 则把它交给链中的下一个转换器
     * @return 转换后的值
     */
    Object convert(Object value, ConvertChain chain);
}
