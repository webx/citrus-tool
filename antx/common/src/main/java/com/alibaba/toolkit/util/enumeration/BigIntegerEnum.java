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
package com.alibaba.toolkit.util.enumeration;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 类型安全的枚举类型, 代表一个超长整数.
 *
 * @version $Id: BigIntegerEnum.java,v 1.1 2003/07/03 07:26:20 baobao Exp $
 * @author Michael Zhou
 */
public abstract class BigIntegerEnum extends Enum {
    static final long serialVersionUID = 3407019802348379119L;

    /**
     * 创建一个枚举量.
     *
     * @param name 枚举量的名称
     */
    protected BigIntegerEnum(String name) {
        super(name);
    }

    /**
     * 创建一个枚举量.
     *
     * @param name  枚举量的名称
     * @param value 枚举量的超长整数值
     */
    protected BigIntegerEnum(String name, int value) {
        super(name, new BigInteger(String.valueOf(value)));
    }

    /**
     * 创建一个枚举量.
     *
     * @param name  枚举量的名称
     * @param value 枚举量的超长整数值
     */
    protected BigIntegerEnum(String name, long value) {
        super(name, new BigInteger(String.valueOf(value)));
    }

    /**
     * 创建一个枚举量.
     *
     * @param name  枚举量的名称
     * @param value 枚举量的超长整数值
     */
    protected BigIntegerEnum(String name, String value) {
        super(name, new BigInteger(value));
    }

    /**
     * 创建一个枚举量.
     *
     * @param name  枚举量的名称
     * @param value 枚举量的超长整数值
     */
    protected BigIntegerEnum(String name, BigInteger value) {
        super(name, value);
    }

    /**
     * 创建一个枚举量.
     *
     * @param name  枚举量的名称
     * @param value 枚举量的超长整数值
     */
    protected BigIntegerEnum(String name, BigDecimal value) {
        super(name, value.toBigInteger());
    }

    /**
     * 创建一个枚举类型的<code>EnumType</code>.
     *
     * @return 枚举类型的<code>EnumType</code>
     */
    protected static Object createEnumType() {
        return new EnumType() {
            protected Class getUnderlyingClass() {
                return BigInteger.class;
            }

            protected Object getNextValue(Object value, boolean flagMode) {
                if (value == null) {
                    return flagMode ? BigInteger.ONE
                                    : BigInteger.ZERO; // 默认起始值
                }

                if (flagMode) {
                    return ((BigInteger) value).shiftLeft(1); // 位模式
                } else {
                    return ((BigInteger) value).add(BigInteger.ONE);
                }
            }

            protected boolean isZero(Object value) {
                return ((BigInteger) value).equals(BigInteger.ZERO);
            }

            protected boolean isPowerOfTwo(Object value) {
                BigInteger bintValue = (BigInteger) value;
                int        bitIndex = bintValue.getLowestSetBit();

                if (bitIndex < 0) {
                    return false;
                }

                return bintValue.clearBit(bitIndex).equals(BigInteger.ZERO);
            }
        };
    }

    /**
     * 实现<code>Number</code>类, 取得整数值.
     *
     * @return 整数值
     */
    public int intValue() {
        return ((BigInteger) getValue()).intValue();
    }

    /**
     * 实现<code>Number</code>类, 取得长整数值.
     *
     * @return 长整数值
     */
    public long longValue() {
        return ((BigInteger) getValue()).longValue();
    }

    /**
     * 实现<code>Number</code>类, 取得<code>double</code>值.
     *
     * @return <code>double</code>值
     */
    public double doubleValue() {
        return ((BigInteger) getValue()).doubleValue();
    }

    /**
     * 实现<code>Number</code>类, 取得<code>float</code>值.
     *
     * @return <code>float</code>值
     */
    public float floatValue() {
        return ((BigInteger) getValue()).floatValue();
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成十六进制整数字符串.
     *
     * @return 十六进制整数字符串
     */
    public String toHexString() {
        return ((BigInteger) getValue()).toString(RADIX_HEX);
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成八进制整数字符串.
     *
     * @return 八进制整数字符串
     */
    public String toOctalString() {
        return ((BigInteger) getValue()).toString(RADIX_OCT);
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成二进制整数字符串.
     *
     * @return 二进制整数字符串
     */
    public String toBinaryString() {
        return ((BigInteger) getValue()).toString(RADIX_BIN);
    }
}
