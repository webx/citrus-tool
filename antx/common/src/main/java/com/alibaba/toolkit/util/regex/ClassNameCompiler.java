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

import org.apache.oro.text.regex.MalformedPatternException;

/**
 * 这个类将一个包含通配符的类名, 编译成Perl5的正则表达式.  格式描述如下:
 *
 * <ul>
 * <li>
 * 合法的<em>类名字符</em>包括: 字母/数字/下划线/'$';
 * </li>
 * <li>
 * 合法的<em>类名分隔符</em>为小数点".";
 * </li>
 * <li>
 * "＊"代表0个或多个<em>类名字符</em>;
 * </li>
 * <li>
 * "？"代表1个<em>类名字符</em>;
 * </li>
 * <li>
 * "＊＊"代表0个或多个<em>类名字符</em>或<em>类名分隔符</em>;
 * </li>
 * <li>
 * 不能连续出现3个"＊";
 * </li>
 * <li>
 * 不能连续出现2个<em>类名分隔符</em>;
 * </li>
 * <li>
 * "＊＊"的前后只能是<em>类名分隔符</em>.
 * </li>
 * </ul>
 *
 * 转换后的正则表达式, 对每一个通配符建立<em>引用变量</em>, 依次为<code>$1</code>, <code>$2</code>, ...
 *
 * @version $Id: ClassNameCompiler.java,v 1.1 2003/07/03 07:26:34 baobao Exp $
 * @author Michael Zhou
 */
public class ClassNameCompiler extends Perl5CompilerWrapper {
    /** 强制从头匹配 */
    public static final int MATCH_PREFIX = 0x1;

    // 私有常量
    private static final char   DOT                          = '.';
    private static final char   UNDERSCORE                   = '_';
    private static final char   DOLLAR                       = '$';
    private static final char   STAR                         = '*';
    private static final char   QUESTION                     = '?';
    private static final String REGEX_MATCH_PREFIX           = "^";
    private static final String REGEX_WORD_BOUNDARY          = "\\b";
    private static final String REGEX_DOT                    = "\\.";
    private static final String REGEX_DOT_NO_DUP             = "\\.(?!\\.)";
    private static final String REGEX_CLASS_NAME_CHAR        = "[\\w\\$]";
    private static final String REGEX_CLASS_NAME_SINGLE_CHAR = "(" + REGEX_CLASS_NAME_CHAR + ")";
    private static final String REGEX_CLASS_NAME             = "(" + REGEX_CLASS_NAME_CHAR + "*)";
    private static final String REGEX_CLASS_NAME_FULL        =
            "(" + REGEX_CLASS_NAME_CHAR + "+(?:" + REGEX_DOT_NO_DUP + REGEX_CLASS_NAME_CHAR
            + "*)*(?=" + REGEX_DOT + "|$)|)" + REGEX_DOT + "?";

    // 上一个token的状态
    private static final int LAST_TOKEN_START       = 0;
    private static final int LAST_TOKEN_DOT         = 1;
    private static final int LAST_TOKEN_CLASS_NAME  = 2;
    private static final int LAST_TOKEN_STAR        = 3;
    private static final int LAST_TOKEN_DOUBLE_STAR = 4;
    private static final int LAST_TOKEN_QUESTION    = 5;

    /**
     * 将包含通配符的类名, 编译成perl5正则表达式.
     *
     * @param pattern  要编译的类名
     * @param options  位标志
     *
     * @return Perl5正则表达式字符串
     *
     * @throws MalformedPatternException  如果类名字符串格式不正确
     */
    public String toPerl5Regex(char[] pattern, int options)
            throws MalformedPatternException {
        int          lastToken = LAST_TOKEN_START;
        StringBuffer buffer = new StringBuffer(pattern.length * 2);

        boolean      matchPrefix = (options & MATCH_PREFIX) != 0;

        if (matchPrefix) {
            buffer.append(REGEX_MATCH_PREFIX);
        }

        for (int i = 0; i < pattern.length; i++) {
            char ch = pattern[i];

            switch (ch) {
                case DOT:

                    // dot后面不能是dot, dot不能作为字符串的开始
                    if ((lastToken == LAST_TOKEN_DOT) || (lastToken == LAST_TOKEN_START)) {
                        throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                    }

                    // 因为**已经包括了dot, 所以不需要额外地匹配dot
                    if (lastToken != LAST_TOKEN_DOUBLE_STAR) {
                        buffer.append(REGEX_DOT_NO_DUP);
                    }

                    lastToken = LAST_TOKEN_DOT;
                    break;

                case STAR:

                    int j = i + 1;

                    if ((j < pattern.length) && (pattern[j] == STAR)) {
                        i = j;

                        // **前面只能是dot
                        if ((lastToken != LAST_TOKEN_START) && (lastToken != LAST_TOKEN_DOT)) {
                            throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                        }

                        lastToken = LAST_TOKEN_DOUBLE_STAR;
                        buffer.append(REGEX_CLASS_NAME_FULL);
                    } else {
                        // *前面不能是*或**
                        if ((lastToken == LAST_TOKEN_STAR) || (lastToken == LAST_TOKEN_DOUBLE_STAR)) {
                            throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                        }

                        lastToken = LAST_TOKEN_STAR;
                        buffer.append(REGEX_CLASS_NAME);
                    }

                    break;

                case QUESTION:
                    lastToken = LAST_TOKEN_QUESTION;
                    buffer.append(REGEX_CLASS_NAME_SINGLE_CHAR);
                    break;

                default:

                    // **后只能是dot
                    if (lastToken == LAST_TOKEN_DOUBLE_STAR) {
                        throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                    }

                    if (Character.isLetterOrDigit(ch) || (ch == UNDERSCORE)) {
                        // 加上word边界, 进行整字匹配
                        if (lastToken == LAST_TOKEN_START) {
                            buffer.append(REGEX_WORD_BOUNDARY).append(ch); // 前边界
                        } else if ((i + 1) == pattern.length) {
                            buffer.append(ch).append(REGEX_WORD_BOUNDARY); // 后边界
                        } else {
                            buffer.append(ch);
                        }
                    } else if (ch == DOLLAR) {
                        buffer.append(ESCAPE_CHAR).append(DOLLAR);
                    } else {
                        throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                    }

                    lastToken = LAST_TOKEN_CLASS_NAME;
            }
        }

        return buffer.toString();
    }
}
