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

package com.alibaba.toolkit.util.exception;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.alibaba.toolkit.util.enumeration.Enum;
import com.alibaba.toolkit.util.resourcebundle.MessageBuilder;

/**
 * 产生携带错误代码的异常的错误信息.
 * 
 * @version $Id: ErrorCodeMessageBuilder.java,v 1.1 2003/07/03 07:26:22 baobao
 *          Exp $
 * @author Michael Zhou
 */
public class ErrorCodeMessageBuilder extends MessageBuilder {
    protected static final String STRING_ERROR_CODE_PREFIX = "ERR-";
    protected static final String STRING_ERROR_CODE_SUFFIX = ": ";

    /**
     * 创建一个<code>ErrorCodeMessageBuilder</code>.
     * 
     * @param bundleName 错误信息的资源束名称
     * @param errorCode 错误代码
     * @throws MissingResourceException 指定bundle未找到, 或创建bundle错误
     */
    public ErrorCodeMessageBuilder(String bundleName, Enum errorCode) {
        super(bundleName, errorCode);
    }

    /**
     * 创建一个<code>ErrorCodeMessageBuilder</code>.
     * 
     * @param bundle 错误信息的资源束
     * @param errorCode 错误代码
     */
    public ErrorCodeMessageBuilder(ResourceBundle bundle, Enum errorCode) {
        super(bundle, errorCode);
    }

    /**
     * 取得错误信息.
     * 
     * @param message 错误信息
     * @return 错误信息
     */
    public String toString(String message) {
        return new StringBuffer(STRING_ERROR_CODE_PREFIX).append(((Enum) key).toHexString())
                .append(STRING_ERROR_CODE_SUFFIX).append(getMessage()).toString();
    }
}
