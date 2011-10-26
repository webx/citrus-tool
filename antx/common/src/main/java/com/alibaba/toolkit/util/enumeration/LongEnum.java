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
 * 类型安全的枚举类型, 代表一个长整数.
 *
 * @version $Id: LongEnum.java,v 1.1 2003/07/03 07:26:21 baobao Exp $
 * @author Michael Zhou
 */
public abstract class LongEnum extends Enum {
    private static final long serialVersionUID = 8152633183977823349L;

    /**
     * 创建一个枚举量.
     *
     * @param name 枚举量的名称
     */
    protected LongEnum(String name) {
        super(name);
    }

    /**
     * 创建一个枚举量.
     *
     * @param name  枚举量的名称
     * @param value 枚举量的长整数值
     */
    protected LongEnum(String name, long value) {
        super(name, new Long(value));
    }

    /**
     * 创建一个枚举类型的<code>EnumType</code>.
     *
     * @return 枚举类型的<code>EnumType</code>
     */
    protected static Object createEnumType() {
        return new EnumType() {
            protected Class getUnderlyingClass() {
                return Long.class;
            }

            protected Object getNextValue(Object value, boolean flagMode) {
                if (value == null) {
                    return flagMode ? new Long(1)
                                    : new Long(0); // 默认起始值
                }

                long longValue = ((Long) value).longValue();

                if (flagMode) {
                    return new Long(longValue << 1); // 位模式
                } else {
                    return new Long(longValue + 1);
                }
            }

            protected boolean isZero(Object value) {
                return ((Long) value).longValue() == 0L;
            }

            protected boolean isPowerOfTwo(Object value) {
                long longValue = ((Long) value).longValue();

                if (longValue == 0L) {
                    return false;
                }

                while ((longValue & 1L) == 0L) {
                    longValue = longValue >>> 1;
                }

                return longValue == 1L;
            }
        };
    }

    /**
     * 实现<code>Number</code>类, 取得整数值.
     *
     * @return 整数值
     */
    public int intValue() {
        return ((Long) getValue()).intValue();
    }

    /**
     * 实现<code>Number</code>类, 取得长整数值.
     *
     * @return 长整数值
     */
    public long longValue() {
        return ((Long) getValue()).longValue();
    }

    /**
     * 实现<code>Number</code>类, 取得<code>double</code>值.
     *
     * @return <code>double</code>值
     */
    public double doubleValue() {
        return ((Long) getValue()).doubleValue();
    }

    /**
     * 实现<code>Number</code>类, 取得<code>float</code>值.
     *
     * @return <code>float</code>值
     */
    public float floatValue() {
        return ((Long) getValue()).floatValue();
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成十六进制整数字符串.
     *
     * @return 十六进制整数字符串
     */
    public String toHexString() {
        return Long.toHexString(((Long) getValue()).intValue());
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成八进制整数字符串.
     *
     * @return 八进制整数字符串
     */
    public String toOctalString() {
        return Long.toOctalString(((Long) getValue()).intValue());
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成二进制整数字符串.
     *
     * @return 二进制整数字符串
     */
    public String toBinaryString() {
        return Long.toBinaryString(((Long) getValue()).intValue());
    }
}
