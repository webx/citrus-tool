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

package com.alibaba.toolkit.util.enumeration;

/**
 * 基于整数的数字接口.
 *
 * @version $Id: IntegralNumber.java,v 1.1 2003/07/03 07:26:21 baobao Exp $
 * @author Michael Zhou
 */
public interface IntegralNumber {
    int RADIX_HEX = 16;
    int RADIX_OCT = 8;
    int RADIX_BIN = 2;

    /**
     * 取得整数值
     *
     * @return 整数值
     */
    int intValue();

    /**
     * 取得长整数值
     *
     * @return 长整数值
     */
    long longValue();

    /**
     * 取得浮点值
     *
     * @return 浮点值
     */
    float floatValue();

    /**
     * 取得取得double值
     *
     * @return double值
     */
    double doubleValue();

    /**
     * 取得byte值
     *
     * @return byte值
     */
    byte byteValue();

    /**
     * 取得short值
     *
     * @return short值
     */
    short shortValue();

    /**
     * 转换成十六进制整数字符串.
     *
     * @return 十六进制整数字符串
     */
    String toHexString();

    /**
     * 转换成八进制整数字符串.
     *
     * @return 八进制整数字符串
     */
    String toOctalString();

    /**
     * 转换成二进制整数字符串.
     *
     * @return 二进制整数字符串
     */
    String toBinaryString();
}
