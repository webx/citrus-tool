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

import com.alibaba.toolkit.util.exception.ChainedRuntimeException;

/**
 * 表示转换失败的异常. 转换失败时, 可以指定一个建议的默认值.
 *
 * @author Michael Zhou
 * @version $Id: ConvertFailedException.java,v 1.1 2003/07/03 07:26:36 baobao
 *          Exp $
 */
public class ConvertFailedException extends ChainedRuntimeException {
    private static final long serialVersionUID = -3145089557163861714L;
    private Object defaultValue;
    private boolean defaultValueSet = false;

    /** 构造一个空的异常. */
    public ConvertFailedException() {
        super();
    }

    /**
     * 构造一个异常, 指明异常的详细信息.
     *
     * @param message 详细信息
     */
    public ConvertFailedException(String message) {
        super(message);
    }

    /**
     * 构造一个异常, 指明引起这个异常的起因.
     *
     * @param cause 异常的起因
     */
    public ConvertFailedException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造一个异常, 指明引起这个异常的起因.
     *
     * @param message 详细信息
     * @param cause   异常的起因
     */
    public ConvertFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 设置建议的默认值.
     *
     * @param defaultValue 默认值
     * @return 异常本身
     */
    public ConvertFailedException setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        this.defaultValueSet = true;
        return this;
    }

    /**
     * 取得默认值.
     *
     * @return 默认值对象
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * 是否设置了默认值.
     *
     * @return 如果设置了默认值, 则返回<code>true</code>
     */
    public boolean isDefaultValueSet() {
        return defaultValueSet;
    }
}
