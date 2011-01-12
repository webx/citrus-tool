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
package com.alibaba.toolkit.util.enumeration;

import com.alibaba.toolkit.util.typeconvert.ConvertChain;
import com.alibaba.toolkit.util.typeconvert.ConvertFailedException;
import com.alibaba.toolkit.util.typeconvert.Converter;

/**
 * 将对象转换成<code>Enum</code>.
 *
 * <ul>
 * <li>
 * 如果对象已经是<code>targetType</code>了, 直接返回.
 * </li>
 * <li>
 * 如果对象是字符串, 则试着把它转换成字符串名称所代表的<code>Enum</code>.  如果成功, 则返回.
 * </li>
 * <li>
 * 试着将对象转换成<code>Enum.getUnderlyingClass</code>类型, 如果成功, 则返回对应的<code>Enum</code>.
 * </li>
 * <li>
 * 如果有默认值, 则抛出带默认值的<code>ConvertFailedException</code>
 * </li>
 * <li>
 * 否则, 把对象传递给下一个<code>Converter</code>处理.
 * </li>
 * </ul>
 *
 *
 * @version $Id: EnumConverter.java,v 1.1 2003/07/03 07:26:20 baobao Exp $
 * @author Michael Zhou
 */
public class EnumConverter implements Converter {
    public Object convert(Object value, ConvertChain chain) {
        Class targetType = chain.getTargetType();

        if (targetType.isInstance(value)) {
            return value;
        }

        if (value instanceof String) {
            Enum enumObj = Enum.getEnumByName(targetType, ((String) value).trim());

            if (enumObj != null) {
                return enumObj;
            }
        }

        Enum enumObj = null;

        try {
            Object enumValue = chain.getConvertManager()
                                    .asTypeWithoutDefaultValue(Enum.getUnderlyingClass(targetType),
                                                               value);

            enumObj = Enum.getEnumByValue(targetType, enumValue);
        } catch (ConvertFailedException e) {
            if (e.isDefaultValueSet()) {
                enumObj = Enum.getEnumByValue(targetType, e.getDefaultValue());

                if (enumObj != null) {
                    throw new ConvertFailedException(e).setDefaultValue(enumObj);
                }
            }

            return chain.convert(value);
        }

        if (enumObj == null) {
            throw new ConvertFailedException();
        }

        return enumObj;
    }
}
