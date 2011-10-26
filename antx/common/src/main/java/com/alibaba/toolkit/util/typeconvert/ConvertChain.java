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
 * <p>
 * 代表一个转换链.
 * </p>
 *
 * <p>
 * 将一个对象转换成指定类型时, 转换链中包括多个可能可以实现这个转换的所有转换器. 实行转换时, 如果前一个转换器不能转换这个value, 则将控制交给下一个转换器.
 * </p>
 *
 * @version $Id: ConvertChain.java,v 1.1 2003/07/03 07:26:36 baobao Exp $
 * @author Michael Zhou
 */
public interface ConvertChain {
    /**
     * 取得创建此链的<code>ConvertManager</code>.
     *
     * @return 创建此链的<code>ConvertManager</code>
     */
    ConvertManager getConvertManager();

    /**
     * 取得转换的目标类型.
     *
     * @return 目标类型
     */
    Class getTargetType();

    /**
     * 将控制交给链中的下一个转换器, 转换指定的值到指定的类型.
     *
     * @param value       要转换的值
     *
     * @return 转换的结果
     */
    Object convert(Object value);
}
