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

import java.sql.Timestamp;

/**
 * 将对象转换成<code>java.sql.Timestamp</code>.
 *
 * <ul>
 * <li>
 * 如果对象为<code>null</code>, 则抛出带默认值的<code>ConvertFailedException</code>.
 * </li>
 * <li>
 * 如果对象已经是<code>java.sql.Timestamp</code>了, 直接返回.
 * </li>
 * <li>
 * 如果对象是空字符串, 则抛出带默认值的<code>ConvertFailedException</code>.
 * </li>
 * <li>
 * 如果对象是字符串, 则试着把它转换成<code>java.sql.Timestamp</code>, 格式必须为"yyyy-mm-dd hh:mm:ss.fffffffff".  如果不成功,
 * 则抛出<code>ConvertFailedException</code>.
 * </li>
 * <li>
 * 否则, 把对象传递给下一个<code>Converter</code>处理.
 * </li>
 * </ul>
 *
 *
 * @version $Id: SqlTimestampConverter.java,v 1.1 2003/07/03 07:26:41 baobao Exp $
 * @author Michael Zhou
 */
public class SqlTimestampConverter implements Converter {
    protected static final Timestamp DEFAULT_VALUE = null;

    public Object convert(Object value, ConvertChain chain) {
        if (value == null) {
            throw new ConvertFailedException().setDefaultValue(DEFAULT_VALUE);
        }

        if (value instanceof Timestamp) {
            return value;
        }

        if (value instanceof String) {
            String strValue = ((String) value).trim();

            try {
                return Timestamp.valueOf(strValue);
            } catch (IllegalArgumentException e) {
                if (strValue.length() > 0) {
                    throw new ConvertFailedException(e);
                }

                throw new ConvertFailedException().setDefaultValue(DEFAULT_VALUE);
            }
        }

        return chain.convert(value);
    }
}
