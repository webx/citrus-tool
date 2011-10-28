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
 * 类型安全的枚举类型, 代表一个整数.
 *
 * @version $Id: IntegerEnum.java,v 1.1 2003/07/03 07:26:20 baobao Exp $
 * @author Michael Zhou
 */
public abstract class IntegerEnum extends Enum {
    private static final long serialVersionUID = 343392921439669443L;

    /**
     * 创建一个枚举量.
     *
     * @param name 枚举量的名称
     */
    protected IntegerEnum(String name) {
        super(name);
    }

    /**
     * 创建一个枚举量.
     *
     * @param name  枚举量的名称
     * @param value 枚举量的整数值
     */
    protected IntegerEnum(String name, int value) {
        super(name, new Integer(value));
    }

    /**
     * 创建一个枚举类型的<code>EnumType</code>.
     *
     * @return 枚举类型的<code>EnumType</code>
     */
    protected static Object createEnumType() {
        return new EnumType() {
            protected Class getUnderlyingClass() {
                return Integer.class;
            }

            protected Object getNextValue(Object value, boolean flagMode) {
                if (value == null) {
                    return flagMode ? new Integer(1)
                                    : new Integer(0); // 默认起始值
                }

                int intValue = ((Integer) value).intValue();

                if (flagMode) {
                    return new Integer(intValue << 1); // 位模式
                } else {
                    return new Integer(intValue + 1);
                }
            }

            protected boolean isZero(Object value) {
                return ((Integer) value).intValue() == 0;
            }

            protected boolean isPowerOfTwo(Object value) {
                int intValue = ((Integer) value).intValue();

                if (intValue == 0) {
                    return false;
                }

                while ((intValue & 1) == 0) {
                    intValue = intValue >>> 1;
                }

                return intValue == 1;
            }
        };
    }

    /**
     * 实现<code>Number</code>类, 取得整数值.
     *
     * @return 整数值
     */
    public int intValue() {
        return ((Integer) getValue()).intValue();
    }

    /**
     * 实现<code>Number</code>类, 取得长整数值.
     *
     * @return 长整数值
     */
    public long longValue() {
        return ((Integer) getValue()).longValue();
    }

    /**
     * 实现<code>Number</code>类, 取得<code>double</code>值.
     *
     * @return <code>double</code>值
     */
    public double doubleValue() {
        return ((Integer) getValue()).doubleValue();
    }

    /**
     * 实现<code>Number</code>类, 取得<code>float</code>值.
     *
     * @return <code>float</code>值
     */
    public float floatValue() {
        return ((Integer) getValue()).floatValue();
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成十六进制整数字符串.
     *
     * @return 十六进制整数字符串
     */
    public String toHexString() {
        return Integer.toHexString(((Integer) getValue()).intValue());
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成八进制整数字符串.
     *
     * @return 八进制整数字符串
     */
    public String toOctalString() {
        return Integer.toOctalString(((Integer) getValue()).intValue());
    }

    /**
     * 实现<code>IntegralNumber</code>类, 转换成二进制整数字符串.
     *
     * @return 二进制整数字符串
     */
    public String toBinaryString() {
        return Integer.toBinaryString(((Integer) getValue()).intValue());
    }
}
