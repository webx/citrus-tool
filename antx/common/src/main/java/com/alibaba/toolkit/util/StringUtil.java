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

package com.alibaba.toolkit.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * 和字符串有关的小工具.
 *
 * @version $Id: StringUtil.java,v 1.1 2003/07/03 07:26:15 baobao Exp $
 * @author Michael Zhou
 */
public class StringUtil {
    private static final OutputStream DUMMY_OUTPUT_STREAM = new ByteArrayOutputStream(0);
    private static final String       SYSTEM_CHARSET =
            new OutputStreamWriter(DUMMY_OUTPUT_STREAM).getEncoding();

    /* ============================================================================ */
    /* 以下是有关resource bundle的方法                                              */
    /* ============================================================================ */

    /**
     * 使用<code>MessageFormat</code>格式化字符串.
     *
     * @param bundle  resource bundle
     * @param key     要查找的键
     * @param params  参数表
     *
     * @return key对应的字符串
     *
     * @throws NullPointerException      resource key为<code>null</code>或resource
     *         bundle为<code>null</code>
     * @throws MissingResourceException  指定resource key未找到
     */
    public static String getMessage(ResourceBundle bundle, Object key, Object[] params) {
        String pattern = bundle.getString(key.toString());

        if ((params == null) || (params.length == 0)) {
            return pattern;
        }

        return MessageFormat.format(pattern, params);
    }

    /**
     * 使用<code>MessageFormat</code>格式化字符串.
     *
     * @param bundle  resource bundle
     * @param key     要查找的键
     * @param param1  参数1
     *
     * @return key对应的字符串
     *
     * @throws NullPointerException      resource key为<code>null</code>或resource
     *         bundle为<code>null</code>
     * @throws MissingResourceException  指定resource key未找到
     */
    public static String getMessage(ResourceBundle bundle, Object key, Object param1) {
        return getMessage(bundle, key, new Object[] {
            param1
        });
    }

    /**
     * 使用<code>MessageFormat</code>格式化字符串.
     *
     * @param bundle  resource bundle
     * @param key     要查找的键
     * @param param1  参数1
     * @param param2  参数2
     *
     * @return key对应的字符串
     *
     * @throws NullPointerException      resource key为<code>null</code>或resource
     *         bundle为<code>null</code>
     * @throws MissingResourceException  指定resource key未找到
     */
    public static String getMessage(ResourceBundle bundle, Object key, Object param1, Object param2) {
        return getMessage(bundle, key, new Object[] {
            param1,
            param2
        });
    }

    /**
     * 使用<code>MessageFormat</code>格式化字符串.
     *
     * @param bundle  resource bundle
     * @param key     要查找的键
     * @param param1  参数1
     * @param param2  参数2
     * @param param3  参数3
     *
     * @return key对应的字符串
     *
     * @throws NullPointerException      resource key为<code>null</code>或resource
     *         bundle为<code>null</code>
     * @throws MissingResourceException  指定resource key未找到
     */
    public static String getMessage(ResourceBundle bundle, Object key, Object param1, Object param2,
                                    Object param3) {
        return getMessage(bundle, key, new Object[] {
            param1,
            param2,
            param3
        });
    }

    /**
     * 使用<code>MessageFormat</code>格式化字符串.
     *
     * @param bundle  resource bundle
     * @param key     要查找的键
     * @param param1  参数1
     * @param param2  参数2
     * @param param3  参数3
     * @param param4  参数4
     *
     * @return key对应的字符串
     *
     * @throws NullPointerException      resource key为<code>null</code>或resource
     *         bundle为<code>null</code>
     * @throws MissingResourceException  指定resource key未找到
     */
    public static String getMessage(ResourceBundle bundle, Object key, Object param1, Object param2,
                                    Object param3, Object param4) {
        return getMessage(bundle, key,
                          new Object[] {
            param1,
            param2,
            param3,
            param4
        });
    }

    /**
     * 使用<code>MessageFormat</code>格式化字符串.
     *
     * @param bundle  resource bundle
     * @param key     要查找的键
     * @param param1  参数1
     * @param param2  参数2
     * @param param3  参数3
     * @param param4  参数4
     * @param param5  参数5
     *
     * @return key对应的字符串
     *
     * @throws NullPointerException      resource key为<code>null</code>或resource
     *         bundle为<code>null</code>
     * @throws MissingResourceException  指定resource key未找到
     */
    public static String getMessage(ResourceBundle bundle, Object key, Object param1, Object param2,
                                    Object param3, Object param4, Object param5) {
        return getMessage(bundle, key,
                          new Object[] {
            param1,
            param2,
            param3,
            param4,
            param5
        });
    }

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

    /**
     * 取得系统字符集名称.
     *
     * @return 系统字符集名称
     */
    public static String getSystemCharset() {
        return SYSTEM_CHARSET;
    }

    /**
     * 取得正规的字符集名称, 如果指定字符集不存在, 则抛出<code>UnsupportedEncodingException</code>.
     *
     * @param charset 字符集名称
     *
     * @return 正规的字符集名称
     *
     * @throws UnsupportedEncodingException 如果指定字符集不存在
     */
    public static String getCanonicalCharset(String charset)
            throws UnsupportedEncodingException {
        return new OutputStreamWriter(DUMMY_OUTPUT_STREAM, charset).getEncoding();
    }

    /**
     * 取得正规的字符集名称, 如果指定字符集不存在, 则返回<code>null</code>.
     *
     * @param charset 字符集名称
     *
     * @return 正规的字符集名称, 如果指定字符集不存在, 则返回<code>null</code>
     */
    public static String getCanonicalCharsetQuiet(String charset) {
        try {
            return getCanonicalCharset(charset);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * 展开字符串, 将"$｛"和"｝"中的变量转换成<code>System.getProperties()</code>中的值.
     *
     * @param value       要转换的值
     *
     * @return 展开后的值
     */
    public static String expendProperty(String value) {
        return expendProperty(value, System.getProperties());
    }

    /**
     * 展开字符串, 将"$｛"和"｝"中的变量转换成指定properties中的值.
     *
     * @param value       要转换的值
     * @param properties  可用的变量
     *
     * @return 展开后的值
     */
    public static String expendProperty(String value, Map properties) {
        if (value == null) {
            return null;
        }

        int i = value.indexOf("${", 0);

        if (i == -1) {
            return value;
        }

        StringBuffer buffer = new StringBuffer(value.length());
        int          length = value.length();
        int          j      = 0;

        while (i < length) {
            if (i > j) {
                buffer.append(value.substring(j, i));
                j = i;
            }

            int k;

            for (k = i + 2; (k < length) && (value.charAt(k) != '}'); k++) {
            }

            if (k == length) {
                buffer.append(value.substring(i, k));
                break;
            }

            String propertyName = value.substring(i + 2, k);

            if (propertyName.equals("/")) {
                buffer.append(File.separatorChar);
            } else {
                Object propertyValue = properties.get(propertyName);

                if (propertyValue != null) {
                    buffer.append(propertyValue);
                } else {
                    buffer.append("${").append(propertyName).append("}");
                }
            }

            j = k + 1;
            i = value.indexOf("${", j);

            if (i == -1) {
                if (j < length) {
                    buffer.append(value.substring(j, length));
                }

                break;
            }
        }

        return buffer.toString();
    }

    // add by roy

    /**
     * 根据分割符分割字符串。 Return List,after split Split a String by a splitter(such as ",","hai",...)
     *
     * @param sStr       将要被分割的字符串。
     * @param sSplitter  分割符。
     *
     * @return 一个含有分割好的字符串的List。如果分割失败将返回null,如果字符串中没有包含指定的分割符，
     *         将返回只有一个元素的字符串数组，这个元素就是该字符串本身。如果这个字符串只含有分割符，将返回null。
     */
    public static List splitStr(String sStr, String sSplitter) {
        if ((sStr == null) || (sStr.length() <= 0) || (sSplitter == null)
                || (sSplitter.length() <= 0)) {
            return null;
        }

        StringTokenizer st     = new StringTokenizer(sStr, sSplitter);
        List            result = new ArrayList();

        while (st.hasMoreTokens()) {
            result.add(st.nextToken().trim());
        }

        return result;
    }

    // add by roy
    /**
     * 检验一个String是否是一个数字（例如：7897897->true,789t67 -> false）。
     * 注意：如果是一个负数，将会返回false. Return true if the string represents a number
     *
     * @param str  你想校验的字符串。
     *
     * @return 如果是一个标准的数字，将返回true，否则返回false。
     */
    public static boolean isNum(String str) {
        if ((str == null) || (str.length() <= 0)) {
            return false;
        }

        char[] ch = str.toCharArray();

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(ch[i])) {
                return false;
            }
        }

        return true;
    }

}
