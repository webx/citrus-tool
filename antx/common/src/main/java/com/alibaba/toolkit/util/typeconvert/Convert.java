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

package com.alibaba.toolkit.util.typeconvert;

/**
 * 转换对象类型的工具类, 支持所有primitive类型和数组的转换.
 *
 * @author Michael Zhou
 * @version $Id: Convert.java,v 1.1 2003/07/03 07:26:36 baobao Exp $
 */
public class Convert {
    private static final ConvertManager defaultConvertManager = new ConvertManager();

    /**
     * 将指定值转换成<code>boolean</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static boolean asBoolean(Object value) {
        return defaultConvertManager.asBoolean(value);
    }

    /**
     * 将指定值转换成<code>boolean</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static boolean asBoolean(Object value, boolean defaultValue) {
        return defaultConvertManager.asBoolean(value, defaultValue);
    }

    /**
     * 将指定值转换成<code>byte</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static byte asByte(Object value) {
        return defaultConvertManager.asByte(value);
    }

    /**
     * 将指定值转换成<code>byte</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static byte asByte(Object value, byte defaultValue) {
        return defaultConvertManager.asByte(value, defaultValue);
    }

    /**
     * 将指定值转换成<code>char</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static char asChar(Object value) {
        return defaultConvertManager.asChar(value);
    }

    /**
     * 将指定值转换成<code>char</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static char asChar(Object value, char defaultValue) {
        return defaultConvertManager.asChar(value, defaultValue);
    }

    /**
     * 将指定值转换成<code>double</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static double asDouble(Object value) {
        return defaultConvertManager.asDouble(value);
    }

    /**
     * 将指定值转换成<code>double</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static double asDouble(Object value, double defaultValue) {
        return defaultConvertManager.asDouble(value, defaultValue);
    }

    /**
     * 将指定值转换成<code>float</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static float asFloat(Object value) {
        return defaultConvertManager.asFloat(value);
    }

    /**
     * 将指定值转换成<code>float</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static float asFloat(Object value, float defaultValue) {
        return defaultConvertManager.asFloat(value, defaultValue);
    }

    /**
     * 将指定值转换成<code>int</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static int asInt(Object value) {
        return defaultConvertManager.asInt(value);
    }

    /**
     * 将指定值转换成<code>int</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static int asInt(Object value, int defaultValue) {
        return defaultConvertManager.asInt(value, defaultValue);
    }

    /**
     * 将指定值转换成<code>long</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static long asLong(Object value) {
        return defaultConvertManager.asLong(value);
    }

    /**
     * 将指定值转换成<code>long</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static long asLong(Object value, long defaultValue) {
        return defaultConvertManager.asLong(value, defaultValue);
    }

    /**
     * 将指定值转换成<code>short</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static short asShort(Object value) {
        return defaultConvertManager.asShort(value);
    }

    /**
     * 将指定值转换成<code>short</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static short asShort(Object value, short defaultValue) {
        return defaultConvertManager.asShort(value, defaultValue);
    }

    /**
     * 将指定值转换成<code>String</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public static String asString(Object value) {
        return defaultConvertManager.asString(value);
    }

    /**
     * 将指定值转换成<code>String</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static String asString(Object value, String defaultValue) {
        return defaultConvertManager.asString(value, defaultValue);
    }

    /**
     * 将指定值转换成指定类型.
     *
     * @param targetType 要转换的目标类型
     * @param value      要转换的值
     * @return 转换后的值
     */
    public static Object asType(Object targetType, Object value) {
        return defaultConvertManager.asType(targetType, value);
    }

    /**
     * 将指定值转换成指定类型.
     *
     * @param targetType   要转换的目标类型
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public static Object asType(Object targetType, Object value, Object defaultValue) {
        return defaultConvertManager.asType(targetType, value, defaultValue);
    }
}
