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
package com.alibaba.toolkit.util.regex;

import java.text.MessageFormat;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;

/**
 * 将一种格式的pattern转换成Perl5标准的正则表达式的编译器.
 *
 * @version $Id: Perl5CompilerWrapper.java,v 1.1 2003/07/03 07:26:34 baobao Exp $
 * @author Michael Zhou
 */
public abstract class Perl5CompilerWrapper implements PatternCompiler {
    /** 默认的位标志 */
    public static final int DEFAULT_MASK = 0;

    /** 大小写不敏感标志 */
    public static final int CASE_INSENSITIVE_MASK = 0x1000;

    /** 返回只读的pattern标志 */
    public static final int READ_ONLY_MASK = 0x2000;

    /** Escape字符 */
    protected static final char ESCAPE_CHAR = '\\';

    /** Perl5正则表达式的保留字符 */
    private static final String PERL5_META_CHARS = "*?+[]()|^$.{}\\";

    // 错误信息
    private static final String ERROR_UNEXPECTED_CHAR = "Unexpected \"{0}\" near \"{1}\"";

    /** 内部的perl5编译器 */
    protected final Perl5Compiler compiler = new Perl5Compiler();

    /**
     * 将pattern编译成perl5正则表达式.
     *
     * @param pattern  要编译的pattern
     *
     * @return Perl5正则表达式
     *
     * @throws MalformedPatternException  如果pattern格式不正确
     */
    public Pattern compile(String pattern) throws MalformedPatternException {
        return compile(pattern.toCharArray(), DEFAULT_MASK);
    }

    /**
     * 将pattern编译成perl5正则表达式.
     *
     * @param pattern  要编译的pattern
     * @param options  位标志
     *
     * @return Perl5正则表达式
     *
     * @throws MalformedPatternException  如果pattern格式不正确
     */
    public Pattern compile(String pattern, int options) throws MalformedPatternException {
        return compile(pattern.toCharArray(), options);
    }

    /**
     * 将pattern编译成perl5正则表达式.
     *
     * @param pattern  要编译的pattern
     *
     * @return Perl5正则表达式
     *
     * @throws MalformedPatternException  如果pattern格式不正确
     */
    public Pattern compile(char[] pattern) throws MalformedPatternException {
        return compile(pattern, DEFAULT_MASK);
    }

    /**
     * 将pattern编译成perl5正则表达式.
     *
     * @param pattern  要编译的pattern
     * @param options  位标志
     *
     * @return Perl5正则表达式
     *
     * @throws MalformedPatternException  如果pattern格式不正确
     */
    public Pattern compile(char[] pattern, int options) throws MalformedPatternException {
        int perlOptions = 0;

        if ((options & CASE_INSENSITIVE_MASK) != 0) {
            perlOptions |= Perl5Compiler.CASE_INSENSITIVE_MASK;
        }

        if ((options & READ_ONLY_MASK) != 0) {
            perlOptions |= Perl5Compiler.READ_ONLY_MASK;
        }

        return compiler.compile(toPerl5Regex(pattern, options), perlOptions);
    }

    /**
     * 将pattern编译成perl5正则表达式字符串.
     *
     * @param pattern  要编译的pattern
     * @param options  位标志
     *
     * @return Perl5正则表达式
     *
     * @throws MalformedPatternException  如果pattern格式不正确
     */
    protected abstract String toPerl5Regex(char[] pattern, int options)
            throws MalformedPatternException;

    /**
     * 判断指定字符是否是perl5正则表达式保留的字符
     *
     * @param ch 字符
     *
     * @return 如果是保留字符, 则返回<code>true</code>
     */
    protected boolean isPerl5MetaChar(char ch) {
        return PERL5_META_CHARS.indexOf(ch) != -1;
    }

    /**
     * 取得错误信息.
     *
     * @param pattern  当前处理的pattern
     * @param index    当前处理的pattern的index
     *
     * @return 错误信息
     */
    protected String getDefaultErrorMessage(char[] pattern, int index) {
        return MessageFormat.format(ERROR_UNEXPECTED_CHAR,
                                    new Object[] {
            new Character(pattern[index]),
            new String(pattern, 0, index)
        });
    }
}
