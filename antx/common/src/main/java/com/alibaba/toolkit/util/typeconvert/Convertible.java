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
 * 如果对象实现了此接口, 则在类型转换过程中优先使用此对象指定的转换器.
 *
 * @version $Id: Convertible.java,v 1.1 2003/07/03 07:26:36 baobao Exp $
 * @author Michael Zhou
 */
public interface Convertible {
    /**
     * 取得指定目标类型的转换器.
     *
     * @param targetType  目标类型
     *
     * @return 转换器, 如果不存在合适的转换器, 则返回<code>null</code>
     */
    Converter getConverter(Class targetType);
}
