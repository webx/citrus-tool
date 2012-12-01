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

package com.alibaba.toolkit.util.resourcebundle;

import java.text.MessageFormat;

import com.alibaba.toolkit.util.exception.ChainedException;

/**
 * 表示创建<code>ResourceBundle</code>失败的异常.
 *
 * @author Michael Zhou
 * @version $Id: ResourceBundleCreateException.java,v 1.1 2003/07/03 07:26:35
 *          baobao Exp $
 */
public class ResourceBundleCreateException extends ChainedException {
    private static final long serialVersionUID = -1816609850584933734L;

    /**
     * 构造一个异常, 指明引起这个异常的起因.
     *
     * @param messageId 详细信息ID
     * @param params    详细信息参数
     * @param cause     异常的起因
     */
    public ResourceBundleCreateException(String messageId, Object[] params, Throwable cause) {
        super(MessageFormat.format(messageId, params == null ? new Object[0] : params), cause);
    }
}
