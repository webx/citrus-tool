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
 * 将对象变成<code>Convertible</code>的包装器.
 *
 * @author Michael Zhou
 * @version $Id: ConvertibleWrapper.java,v 1.1 2003/07/03 07:26:36 baobao Exp $
 */
public abstract class ConvertibleWrapper implements Convertible {
    private Object wrappedObject;

    /**
     * 创建包装器.
     *
     * @param wrappedObject 被包装的对象
     */
    public ConvertibleWrapper(Object wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    /**
     * 取得被包装的对象.
     *
     * @return 被包装的对象
     */
    public Object getWrappedObject() {
        return wrappedObject;
    }

    /**
     * 取得<code>Converter</code>.
     *
     * @param targetType 目标类型
     * @return 转换器<code>Converter</code>
     */
    public Converter getConverter(Class targetType) {
        return new Converter() {
            public Object convert(Object value, ConvertChain chain) {
                Class targetType = chain.getTargetType();

                value = preConvert(wrappedObject, targetType);
                return postConvert(targetType, chain.convert(value));
            }
        };
    }

    /**
     * 预转换.
     *
     * @param wrappedObject 被包装的对象
     * @param targetType    目标类型
     * @return 预转换后的对象
     */
    protected Object preConvert(Object wrappedObject, Class targetType) {
        return wrappedObject;
    }

    /**
     * 后转换.
     *
     * @param targetType     目标类型
     * @param convertedValue 转换的结果
     * @return 经过处理的转换结果
     */
    protected Object postConvert(Class targetType, Object convertedValue) {
        return convertedValue;
    }
}
