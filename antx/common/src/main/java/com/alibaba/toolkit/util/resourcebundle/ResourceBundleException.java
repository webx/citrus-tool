/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import com.alibaba.toolkit.util.enumeration.Enum;
import com.alibaba.toolkit.util.exception.ChainedThrowable;
import com.alibaba.toolkit.util.exception.ChainedThrowableDelegate;
import com.alibaba.toolkit.util.exception.ErrorCode;

/**
 * 表示<code>ResourceBundle</code>未找到, 或创建失败的异常.
 *
 * @author Michael Zhou
 * @version $Id: ResourceBundleException.java,v 1.1 2003/07/03 07:26:35 baobao
 *          Exp $
 */
public class ResourceBundleException extends java.util.MissingResourceException implements ChainedThrowable, ErrorCode {
    private static final long             serialVersionUID = -2272722732501708511L;
    private final        ChainedThrowable delegate         = new ChainedThrowableDelegate(this);
    private Throwable cause;
    private Enum      errorCode;

    /**
     * 构造一个异常, 指明引起这个异常的起因.
     *
     * @param messageId  详细信息ID
     * @param params     详细信息参数
     * @param cause      异常的起因
     * @param bundleName bundle名称
     * @param key        resource key
     */
    public ResourceBundleException(String messageId, Object[] params, Throwable cause, String bundleName, Object key) {
        super(MessageFormat.format(messageId, params == null ? new Object[0] : params), bundleName, String.valueOf(key));
        this.cause = cause;
    }

    /**
     * 取得bundle名.
     *
     * @return bundle名
     */
    public String getBundleName() {
        return super.getClassName();
    }

    /**
     * 取得引起这个异常的起因.
     *
     * @return 异常的起因.
     */
    @Override
    public Throwable getCause() {
        return cause;
    }

    /**
     * 取得错误码.
     *
     * @return 错误码
     */
    public Enum getErrorCode() {
        return errorCode;
    }

    /** 打印调用栈到标准错误. */
    @Override
    public void printStackTrace() {
        delegate.printStackTrace();
    }

    /**
     * 打印调用栈到指定输出流.
     *
     * @param stream 输出字节流.
     */
    @Override
    public void printStackTrace(PrintStream stream) {
        delegate.printStackTrace(stream);
    }

    /**
     * 打印调用栈到指定输出流.
     *
     * @param writer 输出字符流.
     */
    @Override
    public void printStackTrace(PrintWriter writer) {
        delegate.printStackTrace(writer);
    }

    /**
     * 打印异常的调用栈, 不包括起因异常的信息.
     *
     * @param writer 打印到输出流
     */
    public void printCurrentStackTrace(PrintWriter writer) {
        super.printStackTrace(writer);
    }
}
