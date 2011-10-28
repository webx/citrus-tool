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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;

import com.alibaba.toolkit.util.typeconvert.ConvertChain;
import com.alibaba.toolkit.util.typeconvert.Converter;
import com.alibaba.toolkit.util.typeconvert.Convertible;

/**
 * 代表一个或多个<code>Flags</code>构成的位集.
 * 
 * @version $Id: FlagSet.java,v 1.1 2003/07/03 07:26:20 baobao Exp $
 * @author Michael Zhou
 */
public abstract class FlagSet implements Flags, Cloneable, Comparable, Serializable, Convertible {
    private static final long serialVersionUID = -5507969553098965333L;
    private final Class enumClass;
    protected transient boolean immutable;

    /**
     * 创建一个位集.
     * 
     * @param enumClass 位集所代表的内部枚举类
     */
    public FlagSet(Class enumClass) {
        this.enumClass = enumClass;

        if (!Enum.class.isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException(MessageFormat.format(EnumConstants.ILLEGAL_CLASS, new Object[] {
                    enumClass.getName(), Enum.class.getName() }));
        }

        if (!Flags.class.isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException(MessageFormat.format(EnumConstants.ILLEGAL_INTERFACE, new Object[] {
                    enumClass.getName(), Flags.class.getName() }));
        }
    }

    /**
     * 取得内部枚举类型.
     * 
     * @return 内部枚举类型
     */
    public Class getEnumClass() {
        return enumClass;
    }

    /**
     * 取得位集的值的类型.
     * 
     * @return 位集的值的类型
     */
    public Class getUnderlyingClass() {
        return Enum.getUnderlyingClass(enumClass);
    }

    /**
     * 设置位集的值, 值的类型由<code>getUnderlyingClass()</code>确定.
     * 
     * @param value 位集的值
     */
    public abstract void setValue(Object value);

    /**
     * 取得位集的值, 值的类型由<code>getUnderlyingClass()</code>确定.
     * 
     * @return 位集的值
     */
    public abstract Object getValue();

    /**
     * 实现<code>Number</code>类, 取得<code>byte</code>值.
     * 
     * @return <code>byte</code>值
     */
    public byte byteValue() {
        return (byte) intValue();
    }

    /**
     * 实现<code>Number</code>类, 取得<code>short</code>值.
     * 
     * @return <code>short</code>值
     */
    public short shortValue() {
        return (short) intValue();
    }

    /**
     * 实现<code>Convertible</code>接口, 取得将当前位集转换成指定<code>targetType</code>的
     * <code>Converter</code>. 转换的规则如下:
     * <ul>
     * <li>如果<code>targetType</code>是字符串, 则返回<code>FlagSet.toString()</code>.</li>
     * <li>否则将位集的值传递到转换链中.</li>
     * </ul>
     * 
     * @param targetType 目标类型
     * @return 将当前位集转换成指定<code>targetType</code>的<code>Converter</code>
     */
    public Converter getConverter(Class targetType) {
        return new Converter() {
            public Object convert(Object value, ConvertChain chain) {
                FlagSet flagSet = (FlagSet) value;
                Class targetType = chain.getTargetType();

                if (String.class.equals(targetType)) {
                    return flagSet.toString();
                }

                return chain.convert(flagSet.getValue());
            }
        };
    }

    /**
     * 复制位集对象.
     * 
     * @return 复制品
     */
    @Override
    public Object clone() {
        FlagSet flagSet = null;

        try {
            flagSet = (FlagSet) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(MessageFormat.format(EnumConstants.CLONE_NOT_SUPPORTED, new Object[] { getClass()
                    .getName() }));
        }

        flagSet.immutable = false;
        return flagSet;
    }

    /**
     * 和另一个位集比较大小, 就是按位集的值比较.
     * 
     * @param other 要比较的位集
     * @return 如果等于<code>0</code>, 表示值相等, 大于<code>0</code>表示当前的位集的值比
     *         <code>otherFlags</code>大, 小于<code>0</code>表示当前的位集的值比
     *         <code>otherFlags</code>小
     */
    public int compareTo(Object other) {
        if (!getClass().equals(other.getClass())) {
            throw new IllegalArgumentException(MessageFormat.format(EnumConstants.COMPARE_TYPE_MISMATCH, new Object[] {
                    getClass().getName(), other.getClass().getName() }));
        }

        FlagSet otherFlagSet = (FlagSet) other;

        if (!enumClass.equals(otherFlagSet.enumClass)) {
            throw new IllegalArgumentException(MessageFormat.format(EnumConstants.COMPARE_UNDERLYING_CLASS_MISMATCH,
                    new Object[] { enumClass.getName(), otherFlagSet.enumClass.getName() }));
        }

        return ((Comparable) getValue()).compareTo(otherFlagSet.getValue());
    }

    /**
     * 比较两个位集是否相等, 即: 类型相同, 内部类相同, 并且值相同.
     * 
     * @param obj 要比较的对象
     * @return 如果相等, 则返回<code>true</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || !getClass().equals(obj.getClass()) || !enumClass.equals(((FlagSet) obj).enumClass)) {
            return false;
        }

        return getValue().equals(((FlagSet) obj).getValue());
    }

    /**
     * 取得位集的hash值. 如果两个位集相同, 则它们的hash值一定相同.
     * 
     * @return hash值
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ enumClass.hashCode() ^ getValue().hashCode();
    }

    /**
     * 取得位集的字符串表示.
     * 
     * @return 位集的字符串表示
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("{");
        String sep = "";

        for (Iterator i = Enum.iterator(enumClass); i.hasNext();) {
            Flags flags = (Flags) i.next();

            if (test(flags)) {
                buffer.append(sep);
                sep = ", ";
                buffer.append(flags);
            }
        }

        buffer.append("}");
        return buffer.toString();
    }

    /**
     * 设置成不可变的位集.
     * 
     * @return 位集本身
     */
    public Flags setImmutable() {
        this.immutable = true;
        return this;
    }

    /**
     * 清除当前位集的全部位.
     * 
     * @return 当前位集
     */
    public abstract Flags clear();

    /**
     * 清除当前位集的指定位, 等效于<code>andNot</code>操作.
     * 
     * @param flags 标志位
     * @return 当前位集
     */
    public Flags clear(Flags flags) {
        checkImmutable();
        return andNot(flags);
    }

    /**
     * 设置当前位集的指定位, 等效于<code>or</code>操作.
     * 
     * @param flags 标志位
     * @return 当前位集
     */
    public Flags set(Flags flags) {
        checkImmutable();
        return or(flags);
    }

    /**
     * 测试当前位集的指定位, 等效于<code>and(flags) != 0</code>.
     * 
     * @param flags 标志位
     * @return 如果指定位被置位, 则返回<code>true</code>
     */
    public abstract boolean test(Flags flags);

    /**
     * 如果是不可变位集, 则创建一个新的位集, 否则返回本身.
     * 
     * @return 位集本身或复制品
     */
    protected FlagSet getFlagSetForModification() {
        if (immutable) {
            return (FlagSet) this.clone();
        } else {
            return this;
        }
    }

    /**
     * 如果是不可变的位集, 则掷出<code>UnsupportedOperationException</code>.
     */
    protected void checkImmutable() {
        if (immutable) {
            throw new UnsupportedOperationException(EnumConstants.FLAG_SET_IS_IMMUTABLE);
        }
    }

    /**
     * 确保<code>flags</code>非空, 并且是<code>Enum</code>或<code>FlagSet</code>类.
     * 
     * @param flags 要判断的对象
     */
    protected void checkFlags(Flags flags) {
        if (flags == null) {
            throw new NullPointerException(EnumConstants.FLAGS_IS_NULL);
        }

        Class flagsClass = flags.getClass();

        if (!enumClass.equals(flagsClass) && !getClass().equals(flagsClass)) {
            throw new IllegalArgumentException(MessageFormat.format(EnumConstants.ILLEGAL_FLAGS_OBJECT, new Object[] {
                    enumClass.getName(), getClass().getName() }));
        }
    }
}
