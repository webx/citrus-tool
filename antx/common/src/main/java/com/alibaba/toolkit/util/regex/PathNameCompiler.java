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

package com.alibaba.toolkit.util.regex;

import org.apache.oro.text.regex.MalformedPatternException;

/**
 * 这个类将一个包含通配符的文件路径, 编译成Perl5的正则表达式. 格式描述如下:
 * <ul>
 * <li>合法的<em>文件名字符</em>包括: 字母/数字/下划线/小数点/短横线;</li>
 * <li>合法的<em>路径分隔符</em>为斜杠"/";</li>
 * <li>"＊"代表0个或多个<em>文件名字符</em>;</li>
 * <li>"？"代表1个<em>文件名字符</em>;</li>
 * <li>"＊＊"代表0个或多个<em>文件名字符</em>或<em>路径分隔符</em>;</li>
 * <li>不能连续出现3个"＊";</li>
 * <li>不能连续出现2个<em>路径分隔符</em>;</li>
 * <li>"＊＊"的前后只能是<em>路径分隔符</em>.</li>
 * </ul>
 * 转换后的正则表达式, 对每一个通配符建立<em>引用变量</em>, 依次为<code>$1</code>, <code>$2</code>, ...
 *
 * @author Michael Zhou
 * @version $Id: PathNameCompiler.java,v 1.1 2003/07/03 07:26:34 baobao Exp $
 */
public class PathNameCompiler extends Perl5CompilerWrapper {
    /** 强制使用绝对路径 */
    public static final int FORCE_ABSOLUTE_PATH = 0x1;

    /** 强制使用相对路径 */
    public static final int FORCE_RELATIVE_PATH = 0x2;

    /** 从头匹配 */
    public static final int FORCE_MATCH_PREFIX = 0x4;

    // 私有常量
    private static final char   SLASH                       = '/';
    private static final char   UNDERSCORE                  = '_';
    private static final char   DASH                        = '-';
    private static final char   DOT                         = '.';
    private static final char   STAR                        = '*';
    private static final char   QUESTION                    = '?';
    private static final String REGEX_MATCH_PREFIX          = "^";
    private static final String REGEX_WORD_BOUNDARY         = "\\b";
    private static final String REGEX_SLASH                 = "\\/";
    private static final String REGEX_SLASH_NO_DUP          = "\\/(?!\\/)";
    private static final String REGEX_FILE_NAME_CHAR        = "[\\w\\-\\.]";
    private static final String REGEX_FILE_NAME_SINGLE_CHAR = "(" + REGEX_FILE_NAME_CHAR + ")";
    private static final String REGEX_FILE_NAME             = "(" + REGEX_FILE_NAME_CHAR + "*)";
    private static final String REGEX_FILE_PATH             = "(" + REGEX_FILE_NAME_CHAR + "+(?:" + REGEX_SLASH_NO_DUP
                                                              + REGEX_FILE_NAME_CHAR + "*)*(?=" + REGEX_SLASH + "|$)|)" + REGEX_SLASH + "?";

    // 上一个token的状态
    private static final int LAST_TOKEN_START       = 0;
    private static final int LAST_TOKEN_SLASH       = 1;
    private static final int LAST_TOKEN_FILE_NAME   = 2;
    private static final int LAST_TOKEN_STAR        = 3;
    private static final int LAST_TOKEN_DOUBLE_STAR = 4;
    private static final int LAST_TOKEN_QUESTION    = 5;

    /**
     * 将包含通配符的路径表达式, 编译成perl5正则表达式.
     *
     * @param pattern 要编译的路径
     * @param options 位标志
     * @return Perl5正则表达式字符串
     * @throws MalformedPatternException 如果路径字符串格式不正确
     */
    @Override
    public String toPerl5Regex(char[] pattern, int options) throws MalformedPatternException {
        int lastToken = LAST_TOKEN_START;
        StringBuffer buffer = new StringBuffer(pattern.length * 2);

        boolean forceMatchPrefix = (options & FORCE_MATCH_PREFIX) != 0;
        boolean forceAbsolutePath = (options & FORCE_ABSOLUTE_PATH) != 0;
        boolean forceRelativePath = (options & FORCE_RELATIVE_PATH) != 0;

        // 如果第一个字符为slash, 或调用者要求forceMatchPrefix, 则从头匹配
        if (forceMatchPrefix || pattern.length > 0 && pattern[0] == SLASH) {
            buffer.append(REGEX_MATCH_PREFIX);
        }

        for (int i = 0; i < pattern.length; i++) {
            char ch = pattern[i];

            if (forceAbsolutePath && lastToken == LAST_TOKEN_START && ch != SLASH) {
                throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
            }

            switch (ch) {
                case SLASH:

                    // slash后面不能是slash, slash不能位于首字符(如果指定了force relative path的话)
                    if (lastToken == LAST_TOKEN_SLASH) {
                        throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                    } else if (forceRelativePath && lastToken == LAST_TOKEN_START) {
                        throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                    }

                    // 因为**已经包括了slash, 所以不需要额外地匹配slash
                    if (lastToken != LAST_TOKEN_DOUBLE_STAR) {
                        buffer.append(REGEX_SLASH_NO_DUP);
                    }

                    lastToken = LAST_TOKEN_SLASH;
                    break;

                case STAR:

                    int j = i + 1;

                    if (j < pattern.length && pattern[j] == STAR) {
                        i = j;

                        // **前面只能是slash
                        if (lastToken != LAST_TOKEN_START && lastToken != LAST_TOKEN_SLASH) {
                            throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                        }

                        lastToken = LAST_TOKEN_DOUBLE_STAR;
                        buffer.append(REGEX_FILE_PATH);
                    } else {
                        // *前面不能是*或**
                        if (lastToken == LAST_TOKEN_STAR || lastToken == LAST_TOKEN_DOUBLE_STAR) {
                            throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                        }

                        lastToken = LAST_TOKEN_STAR;
                        buffer.append(REGEX_FILE_NAME);
                    }

                    break;

                case QUESTION:
                    lastToken = LAST_TOKEN_QUESTION;
                    buffer.append(REGEX_FILE_NAME_SINGLE_CHAR);
                    break;

                default:

                    // **后只能是slash
                    if (lastToken == LAST_TOKEN_DOUBLE_STAR) {
                        throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                    }

                    if (Character.isLetterOrDigit(ch) || ch == UNDERSCORE || ch == DASH) {
                        // 加上word边界, 进行整字匹配
                        if (lastToken == LAST_TOKEN_START) {
                            buffer.append(REGEX_WORD_BOUNDARY).append(ch); // 前边界
                        } else if (i + 1 == pattern.length) {
                            buffer.append(ch).append(REGEX_WORD_BOUNDARY); // 后边界
                        } else {
                            buffer.append(ch);
                        }
                    } else if (ch == DOT) {
                        buffer.append(ESCAPE_CHAR).append(DOT);
                    } else {
                        throw new MalformedPatternException(getDefaultErrorMessage(pattern, i));
                    }

                    lastToken = LAST_TOKEN_FILE_NAME;
            }
        }

        return buffer.toString();
    }
}
