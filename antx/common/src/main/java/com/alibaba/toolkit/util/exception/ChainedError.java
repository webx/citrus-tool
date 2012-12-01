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

import java.io.PrintStream;
import java.io.PrintWriter;

import com.alibaba.toolkit.util.enumeration.Enum;

/**
 * 可嵌套的异常.
 *
 * @author Michael Zhou
 * @version $Id: ChainedError.java,v 1.2 2003/08/07 08:08:59 zyh Exp $
 */
public class ChainedError extends Error implements ChainedThrowable, ErrorCode {
    private static final long             serialVersionUID = 5023000196051785238L;
    private final        ChainedThrowable delegate         = new ChainedThrowableDelegate(this);
    private Throwable cause;
    private Enum      errorCode;

    /** 构造一个空的异常. */
    public ChainedError() {
        super();
    }

    /**
     * 构造一个异常, 指明异常的详细信息.
     *
     * @param message 详细信息
     */
    public ChainedError(String message) {
        super(message);
    }

    /**
     * 构造一个异常, 指明异常的详细信息.
     *
     * @param message   详细信息
     * @param errorCode 错误码
     */
    public ChainedError(String message, Enum errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造一个异常, 指明引起这个异常的起因.
     *
     * @param cause 异常的起因
     */
    public ChainedError(Throwable cause) {
        super(cause == null ? null : cause.getMessage());
        this.cause = cause;
    }

    /**
     * 构造一个异常, 指明引起这个异常的起因.
     *
     * @param message 详细信息
     * @param cause   异常的起因
     */
    public ChainedError(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    /**
     * 构造一个异常, 指明引起这个异常的起因.
     *
     * @param message   详细信息
     * @param cause     异常的起因
     * @param errorCode 错误码
     */
    public ChainedError(String message, Throwable cause, Enum errorCode) {
        super(message);
        this.cause = cause;
        this.errorCode = errorCode;
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
