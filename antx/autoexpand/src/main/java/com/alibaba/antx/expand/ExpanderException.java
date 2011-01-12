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
package com.alibaba.antx.expand;

import com.alibaba.toolkit.util.exception.ChainedRuntimeException;

/**
 * 代表一个expander的异常
 *
 * @author Michael Zhou
 */
public class ExpanderException extends ChainedRuntimeException {
    private static final long serialVersionUID = -6138090297450520233L;

/**
     * 创建一个异常。
     */
    public ExpanderException() {
        super();
    }

/**
     * 创建一个异常。
     *
     * @param message 异常信息
     */
    public ExpanderException(String message) {
        super(message);
    }

/**
     * 创建一个异常。
     *
     * @param message 异常信息
     * @param cause 异常原因
     */
    public ExpanderException(String message, Throwable cause) {
        super(message, cause);
    }

/**
     * 创建一个异常。
     *
     * @param cause 异常原因
     */
    public ExpanderException(Throwable cause) {
        super(cause);
    }
}
