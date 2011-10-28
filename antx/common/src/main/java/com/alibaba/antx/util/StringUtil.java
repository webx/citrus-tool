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

package com.alibaba.antx.util;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 和字符串有关的小工具.
 *
 * @author Michael Zhou
 */
public class StringUtil {
    /**
     * 检查字符串是否为<code>null</code>或空字符串.
     *
     * @param str 要检查的字符串
     *
     * @return 如果为空, 则返回<code>true</code>
     */
    public static boolean isEmpty(String str) {
        return (str == null) || (str.length() == 0);
    }

    public static boolean isEmpty(Object str) {
        return isEmpty(toString(str));
    }

    /**
     * 检查字符串是否为<code>null</code>或空字符串.
     *
     * @param str 要检查的字符串
     *
     * @return 如果为空, 则返回<code>true</code>
     */
    public static boolean isBlank(String str) {
        return (str == null) || (str.trim().length() == 0);
    }

    public static boolean isBlank(Object str) {
        return isBlank(toString(str));
    }

    /**
     * 取得类名，不包括package名。
     *
     * @param clazz 要查看的类
     *
     * @return 短类名
     */
    public static String getShortClassName(Class clazz) {
        return getShortClassName(clazz.getName());
    }

    /**
     * 取得类名，不包括package名。
     *
     * @param className 要查看的类名
     *
     * @return 短类名
     */
    public static String getShortClassName(String className) {
        int index = className.lastIndexOf('.');

        return className.substring(index + 1);
    }

    /**
     * 将字符串按空格和逗号分解.
     *
     * @param str 要分解的字符串
     *
     * @return 字符串数组
     */
    public static String[] split(String str) {
        return split(str, ",");
    }

    public static String join(Object[] array) {
        return join(array, ",");
    }

    /**
     * 将数组中的元素连接成一个字符串。<pre>StringUtil.join(null, *)                = null
     * StringUtil.join([], *)                  = ""StringUtil.join([null], *)              = ""
     * StringUtil.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtil.join(["a", "b", "c"], null)  = "abc"
     * StringUtil.join(["a", "b", "c"], "")    = "abc"
     * StringUtil.join([null, "", "a"], ',')   = ",,a"</pre>
     *
     * @param array 要连接的数组
     * @param separator 分隔符
     *
     * @return 连接后的字符串，如果原数组为<code>null</code>，则返回<code>null</code>
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }

        if (separator == null) {
            separator = "";
        }

        int arraySize = array.length;

        // ArraySize ==  0: Len = 0
        // ArraySize > 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (估计大约所有的字符串都一样长)
        int bufSize = (arraySize == 0) ? 0
                                       : (arraySize * (((array[0] == null) ? 16
                                                                           : array[0].toString()
                                                                                     .length())
                                                      + ((separator != null) ? separator.length()
                                                                             : 0)));

        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if ((separator != null) && (i > 0)) {
                buf.append(separator);
            }

            if (array[i] != null) {
                buf.append(array[i]);
            }
        }

        return buf.toString();
    }

    /**
     * 将字符串按空格和逗号分解.
     *
     * @param str 要分解的字符串
     *
     * @return 字符串数组
     */
    public static String[] splitPath(String str) {
        return split(str, "," + File.pathSeparator);
    }

    /**
     * 将字符串按指定分隔符分解.
     *
     * @param str 要分解的字符串
     * @param delimiters 分隔符
     *
     * @return 字符串数组
     */
    public static String[] split(String str, String delimiters) {
        if ((str == null) || (str.trim().length() == 0)) {
            return new String[0];
        }

        List            tokens    = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(str, delimiters);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();

            if (token.length() > 0) {
                tokens.add(token);
            }
        }

        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

    /**
     * 删除两端空白。
     *
     * @param str 要处理的字符串
     *
     * @return 除去两端空白的字符串，如果字符串为<code>null</code>，则返回空字符串
     */
    public static String trimWhitespace(String str) {
        if (str == null) {
            return "";
        }

        return str.trim();
    }

    /**
     * 删除所有空白。
     *
     * @param str 要处理的字符串
     *
     * @return 除去空白的字符串，如果字符串为<code>null</code>，则返回空字符串
     */
    public static String deleteWhitespace(String str) {
        if (str == null) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (!Character.isWhitespace(ch)) {
                buffer.append(ch);
            }
        }

        return buffer.toString();
    }

    /**
     * 通过将不合法的字符替换成"_", 将不合法的Java Identifier字符转换成合法的ID.
     *
     * @param id 要转换的字符串
     *
     * @return 合法的ID
     */
    public static String getValidIdentifier(String id) {
        return getValidIdentifier(id, null);
    }

    /**
     * 通过替换不合法的字符, 将不合法的Java Identifier字符转换成合法的ID.
     *
     * @param id 要转换的字符串
     * @param replaceInvalid 用来替换不合法字符的字符串, 如果不指定, 则使用默认字符串"_"
     *
     * @return 合法的ID
     */
    public static String getValidIdentifier(String id, String replaceInvalid) {
        if (replaceInvalid == null) {
            replaceInvalid = "_";
        }

        if ((id == null) || (id.length() == 0)) {
            return replaceInvalid;
        }

        boolean      replaced = false;
        StringBuffer buffer   = new StringBuffer(id.length());
        char         c        = id.charAt(0);

        if (Character.isJavaIdentifierStart(c)) {
            buffer.append(c);
        } else {
            buffer.append(replaceInvalid);
            replaced = true;
        }

        for (int i = 1; i < id.length(); i++) {
            c = id.charAt(i);

            if (Character.isJavaIdentifierPart(c)) {
                buffer.append(c);
                replaced = false;
            } else {
                if (!replaced) {
                    replaced = true;
                    buffer.append(replaceInvalid);
                }
            }
        }

        return buffer.toString();
    }

    /**
     * 扩展并左对齐字符串，用空格<code>' '</code>填充右边。<pre>StringUtil.alignLeft(null, *)   = null
     * StringUtil.alignLeft("", 3)     = "   "StringUtil.alignLeft("bat", 3)  = "bat"
     * StringUtil.alignLeft("bat", 5)  = "bat  "StringUtil.alignLeft("bat", 1)  = "bat"
     * StringUtil.alignLeft("bat", -1) = "bat"</pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String alignLeft(String str, int size) {
        return alignLeft(str, size, ' ');
    }

    /**
     * 扩展并左对齐字符串，用指定字符填充右边。<pre>StringUtil.alignLeft(null, *, *)     = null
     * StringUtil.alignLeft("", 3, 'z')     = "zzz"StringUtil.alignLeft("bat", 3, 'z')  = "bat"
     * StringUtil.alignLeft("bat", 5, 'z')  = "batzz"StringUtil.alignLeft("bat", 1, 'z')  = "bat"
     * StringUtil.alignLeft("bat", -1, 'z') = "bat"</pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     * @param padChar 填充字符
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String alignLeft(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }

        int pads = size - str.length();

        if (pads <= 0) {
            return str;
        }

        return alignLeft(str, size, String.valueOf(padChar));
    }

    /**
     * 扩展并左对齐字符串，用指定字符串填充右边。<pre>StringUtil.alignLeft(null, *, *)      = null
     * StringUtil.alignLeft("", 3, "z")      = "zzz"StringUtil.alignLeft("bat", 3, "yz")  = "bat"
     * StringUtil.alignLeft("bat", 5, "yz")  = "batyz"
     * StringUtil.alignLeft("bat", 8, "yz")  = "batyzyzy"
     * StringUtil.alignLeft("bat", 1, "yz")  = "bat"StringUtil.alignLeft("bat", -1, "yz") = "bat"
     * StringUtil.alignLeft("bat", 5, null)  = "bat  "
     * StringUtil.alignLeft("bat", 5, "")    = "bat  "</pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     * @param padStr 填充字符串
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String alignLeft(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }

        if ((padStr == null) || (padStr.length() == 0)) {
            padStr = " ";
        }

        int padLen = padStr.length();
        int strLen = str.length();
        int pads   = size - strLen;

        if (pads <= 0) {
            return str;
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            char[] padding  = new char[pads];
            char[] padChars = padStr.toCharArray();

            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }

            return str.concat(new String(padding));
        }
    }

    /**
     * 扩展并右对齐字符串，用空格<code>' '</code>填充左边。<pre>StringUtil.alignRight(null, *)   = null
     * StringUtil.alignRight("", 3)     = "   "StringUtil.alignRight("bat", 3)  = "bat"
     * StringUtil.alignRight("bat", 5)  = "  bat"StringUtil.alignRight("bat", 1)  = "bat"
     * StringUtil.alignRight("bat", -1) = "bat"</pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String alignRight(String str, int size) {
        return alignRight(str, size, ' ');
    }

    /**
     * 扩展并右对齐字符串，用指定字符填充左边。<pre>StringUtil.alignRight(null, *, *)     = null
     * StringUtil.alignRight("", 3, 'z')     = "zzz"StringUtil.alignRight("bat", 3, 'z')  = "bat"
     * StringUtil.alignRight("bat", 5, 'z')  = "zzbat"StringUtil.alignRight("bat", 1, 'z')  = "bat"
     * StringUtil.alignRight("bat", -1, 'z') = "bat"</pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     * @param padChar 填充字符
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String alignRight(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }

        int pads = size - str.length();

        if (pads <= 0) {
            return str;
        }

        return alignRight(str, size, String.valueOf(padChar));
    }

    /**
     * 扩展并右对齐字符串，用指定字符串填充左边。<pre>StringUtil.alignRight(null, *, *)      = null
     * StringUtil.alignRight("", 3, "z")      = "zzz"StringUtil.alignRight("bat", 3, "yz")  = "bat"
     * StringUtil.alignRight("bat", 5, "yz")  = "yzbat"
     * StringUtil.alignRight("bat", 8, "yz")  = "yzyzybat"
     * StringUtil.alignRight("bat", 1, "yz")  = "bat"StringUtil.alignRight("bat", -1, "yz") = "bat"
     * StringUtil.alignRight("bat", 5, null)  = "  bat"
     * StringUtil.alignRight("bat", 5, "")    = "  bat"</pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     * @param padStr 填充字符串
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String alignRight(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }

        if ((padStr == null) || (padStr.length() == 0)) {
            padStr = " ";
        }

        int padLen = padStr.length();
        int strLen = str.length();
        int pads   = size - strLen;

        if (pads <= 0) {
            return str;
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            char[] padding  = new char[pads];
            char[] padChars = padStr.toCharArray();

            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }

            return new String(padding).concat(str);
        }
    }

    /**
     * 扩展并居中字符串，用空格<code>' '</code>填充两边。<pre>StringUtil.center(null, *)   = null
     * StringUtil.center("", 4)     = "    "StringUtil.center("ab", -1)  = "ab"
     * StringUtil.center("ab", 4)   = " ab "StringUtil.center("abcd", 2) = "abcd"
     * StringUtil.center("a", 4)    = " a  "</pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String center(String str, int size) {
        return center(str, size, ' ');
    }

    /**
     * 扩展并居中字符串，用指定字符填充两边。<pre>StringUtil.center(null, *, *)     = null
     * StringUtil.center("", 4, ' ')     = "    "StringUtil.center("ab", -1, ' ')  = "ab"
     * StringUtil.center("ab", 4, ' ')   = " ab "StringUtil.center("abcd", 2, ' ') = "abcd"
     * StringUtil.center("a", 4, ' ')    = " a  "StringUtil.center("a", 4, 'y')    = "yayy"</pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     * @param padChar 填充字符
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String center(String str, int size, char padChar) {
        if ((str == null) || (size <= 0)) {
            return str;
        }

        int strLen = str.length();
        int pads   = size - strLen;

        if (pads <= 0) {
            return str;
        }

        str = alignRight(str, strLen + (pads / 2), padChar);
        str = alignLeft(str, size, padChar);
        return str;
    }

    /**
     * 扩展并居中字符串，用指定字符串填充两边。<pre>StringUtil.center(null, *, *)     = null
     * StringUtil.center("", 4, " ")     = "    "StringUtil.center("ab", -1, " ")  = "ab"
     * StringUtil.center("ab", 4, " ")   = " ab "StringUtil.center("abcd", 2, " ") = "abcd"
     * StringUtil.center("a", 4, " ")    = " a  "StringUtil.center("a", 4, "yz")   = "yayz"
     * StringUtil.center("abc", 7, null) = "  abc  "StringUtil.center("abc", 7, "")   = "  abc  "
     * </pre>
     *
     * @param str 要对齐的字符串
     * @param size 扩展字符串到指定宽度
     * @param padStr 填充字符串
     *
     * @return 扩展后的字符串，如果字符串为<code>null</code>，则返回<code>null</code>
     */
    public static String center(String str, int size, String padStr) {
        if ((str == null) || (size <= 0)) {
            return str;
        }

        if ((padStr == null) || (padStr.length() == 0)) {
            padStr = " ";
        }

        int strLen = str.length();
        int pads   = size - strLen;

        if (pads <= 0) {
            return str;
        }

        str = alignRight(str, strLen + (pads / 2), padStr);
        str = alignLeft(str, size, padStr);
        return str;
    }

    /**
     * 去除数据中的空值。
     *
     * @param strs 字符串数组
     *
     * @return 整理后的数组
     */
    public static String[] trimStringArray(String[] strs) {
        if (strs == null) {
            return new String[0];
        }

        List strList = new ArrayList(strs.length);

        for (int i = 0; i < strs.length; i++) {
            String str = StringUtil.trimWhitespace(strs[i]);

            if (str.length() > 0) {
                strList.add(str);
            }
        }

        return (String[]) strList.toArray(new String[strList.size()]);
    }

    public static String toString(Object obj) {
        return (obj == null) ? ""
                             : obj.toString();
    }
}
