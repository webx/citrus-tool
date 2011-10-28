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

package com.alibaba.toolkit.util.typeconvert.converters;

import com.alibaba.toolkit.util.typeconvert.ConvertChain;
import com.alibaba.toolkit.util.typeconvert.ConvertFailedException;
import com.alibaba.toolkit.util.typeconvert.Converter;

/**
 * 将对象转换成字符串.
 * <ul>
 * <li>如果对象为<code>null</code>, 则抛出带默认值的<code>ConvertFailedException</code>.</li>
 * <li>如果对象已经是字符串, 直接返回.</li>
 * <li>如果对象是字符数组, 则将它组合成字符串.</li>
 * <li>否则返回<code>toString()</code>.</li>
 * </ul>
 * 
 * @version $Id: StringConverter.java,v 1.1 2003/07/03 07:26:41 baobao Exp $
 * @author Michael Zhou
 */
public class StringConverter implements Converter {
    protected static final String DEFAULT_VALUE = "";

    public Object convert(Object value, ConvertChain chain) {
        if (value == null) {
            throw new ConvertFailedException().setDefaultValue(DEFAULT_VALUE);
        }

        if (value instanceof String) {
            return value;
        }

        if (value.getClass().isArray()) {
            if (value.getClass().getComponentType().equals(Character.TYPE)) {
                return new String((char[]) value);
            }
        }

        return value.toString();
    }
}
