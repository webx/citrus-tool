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

import com.alibaba.toolkit.util.collection.ArrayHashMap;
import com.alibaba.toolkit.util.collection.ListMap;
import com.alibaba.toolkit.util.typeconvert.ConvertChain;
import com.alibaba.toolkit.util.typeconvert.Converter;
import com.alibaba.toolkit.util.typeconvert.Convertible;

import java.io.InvalidClassException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.text.MessageFormat;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 类型安全的枚举类型.
 *
 * @version $Id: Enum.java,v 1.1 2003/07/03 07:26:20 baobao Exp $
 * @author Michael Zhou
 */
public abstract class Enum implements IntegralNumber, Comparable, Serializable,
                                                     Convertible {
    private static final long serialVersionUID = -3420208858441821772L;
    private static final Map  entries = new WeakHashMap();
    private final String      name;
    private final Object      value;

    /**
     * 创建一个枚举量.  该枚举量被赋予一个自动产生的值.  这个值取决于<code>Enum</code>的类型, 一般是递增的.
     * 如果<code>Enum</code>类实现了<code>Flags</code>接口, 则这个值是倍增的(左移).
     *
     * @param name 枚举量的名称
     */
    protected Enum(String name) {
        this(name, null, false);
    }

    /**
     * 创建一个枚举量, 并赋予指定的值.
     *
     * @param name  枚举量的名称
     * @param value 枚举量的值, 这个值不能为<code>null</code>
     */
    protected Enum(String name, Object value) {
        this(name, value, true);
    }

    /**
     * 创建一个枚举量.
     *
     * @param name      枚举量的名称
     * @param value     枚举量的值
     * @param withValue 如果是<code>true</code>, 则该枚举量被赋予指定的值, 否则该枚举量将被赋予一个自动产生的值
     */
    private Enum(String name, Object value, boolean withValue) {
        if ((name == null) || ((name = name.trim()).length() == 0)) {
            throw new IllegalArgumentException(EnumConstants.ENUM_NAME_IS_EMPTY);
        }

        if (withValue && (value == null)) {
            throw new NullPointerException(EnumConstants.ENUM_VALUE_IS_NULL);
        }

        this.name = name;

        Class    enumClass = getClass();
        EnumType enumType = getEnumType(enumClass);
        boolean  flagMode = this instanceof Flags;

        if (withValue) {
            this.value = enumType.setValue(value, flagMode);
        } else {
            this.value = enumType.getNextValue(flagMode);
        }

        if (enumType.nameMap.containsKey(name)) {
            throw new IllegalArgumentException(MessageFormat.format(
                                                       EnumConstants.DUPLICATED_ENUM_NAME,
                                                       new Object[] {
                name,
                enumClass.getName()
            }));
        }

        enumType.nameMap.put(name, this);

        // 将enum加入valueMap, 如果有多个enum的值相同, 则取第一个
        if (!enumType.valueMap.containsKey(this.value)) {
            enumType.valueMap.put(this.value, this);
        }

        // 如果是flag模式, 则将当前enum加入全集
        if (flagMode) {
            if (enumType.fullSet == null) {
                try {
                    enumType.fullSet = createFlagSet(enumClass);
                } catch (UnsupportedOperationException e) {
                }
            }

            if (enumType.fullSet != null) {
                enumType.fullSet.set((Flags) this);
            }
        }
    }

    /**
     * 取得<code>Enum</code>值的类型.
     *
     * @param enumClass  枚举类型
     *
     * @return <code>Enum</code>值的类型
     */
    public static Class getUnderlyingClass(Class enumClass) {
        return getEnumType(enumClass).getUnderlyingClass();
    }

    /**
     * 判断指定名称的枚举量是否被定义.
     *
     * @param enumClass  枚举类型
     * @param name       枚举量的名称
     *
     * @return 如果存在, 则返回<code>true</code>
     */
    public static boolean isNameDefined(Class enumClass, String name) {
        return getEnumType(enumClass).nameMap.containsKey(name);
    }

    /**
     * 判断指定值的枚举量是否被定义.
     *
     * @param enumClass  枚举类型
     * @param value      枚举量的值
     *
     * @return 如果存在, 则返回<code>true</code>
     */
    public static boolean isValueDefined(Class enumClass, Object value) {
        return getEnumType(enumClass).valueMap.containsKey(value);
    }

    /**
     * 取得指定名称的枚举量.
     *
     * @param enumClass  枚举类型
     * @param name       枚举量的名称
     *
     * @return 枚举量, 如果不存在, 则返回<code>null</code>
     */
    public static Enum getEnumByName(Class enumClass, String name) {
        return (Enum) getEnumType(enumClass).nameMap.get(name);
    }

    /**
     * 取得指定值的枚举量.
     *
     * @param enumClass  枚举类型
     * @param value      枚举量的值
     *
     * @return 枚举量, 如果不存在, 则返回<code>null</code>
     */
    public static Enum getEnumByValue(Class enumClass, Object value) {
        return (Enum) getEnumType(enumClass).valueMap.get(value);
    }

    /**
     * 取得指定类型的所有枚举量的<code>Map</code>, 此<code>Map</code>是有序的.
     *
     * @param enumClass 枚举类型
     *
     * @return 指定类型的所有枚举量的<code>Map</code>
     */
    public static Map getEnumMap(Class enumClass) {
        return Collections.unmodifiableMap(getEnumType(enumClass).nameMap);
    }

    /**
     * 取得指定类型的所有枚举量的<code>Iterator</code>.
     *
     * @param enumClass 枚举类型
     *
     * @return 指定类型的所有枚举量的<code>Iterator</code>
     */
    public static Iterator iterator(Class enumClass) {
        return getEnumMap(enumClass).values().iterator();
    }

    /**
     * 创建和指定枚举类对应的空位集.
     *
     * @param enumClass  枚举类
     *
     * @return 空位集
     */
    public static FlagSet createFlagSet(Class enumClass) {
        if (!(Flags.class.isAssignableFrom(enumClass))) {
            throw new UnsupportedOperationException(MessageFormat.format(
                                                            EnumConstants.ENUM_IS_NOT_A_FLAG,
                                                            new Object[] {
                enumClass.getName()
            }));
        }

        EnumType enumType = getEnumType(enumClass);

        if (enumType.flagSetClassExists && (enumType.flagSetClass == null)) {
            enumType.flagSetClass = findStaticInnerClass(enumClass,
                                                         EnumConstants.FLAG_SET_INNER_CLASS_NAME,
                                                         FlagSet.class);

            if (enumType.flagSetClass == null) {
                enumType.flagSetClassExists = false;
            }
        }

        if (enumType.flagSetClassExists && (enumType.flagSetClass != null)) {
            try {
                return (FlagSet) enumType.flagSetClass.newInstance();
            } catch (IllegalAccessException e) {
            } catch (InstantiationException e) {
            }
        }

        throw new UnsupportedOperationException(MessageFormat.format(
                                                        EnumConstants.CREATE_FLAG_SET_IS_UNSUPPORTED,
                                                        new Object[] {
            enumClass.getName()
        }));
    }

    /**
     * 创建全集.
     *
     * @param enumClass  枚举类型
     *
     * @return 当前枚举类型的全集
     */
    public static FlagSet createFullSet(Class enumClass) {
        FlagSet  flagSet  = createFlagSet(enumClass);
        EnumType enumType = getEnumType(enumClass);

        if ((flagSet != null) && (enumType.fullSet != null)) {
            flagSet.set(enumType.fullSet);
        }

        return flagSet;
    }

    /**
     * 取得指定类的<code>ClassLoader</code>对应的entry表.
     *
     * @param enumClass  <code>Enum</code>类
     *
     * @return entry表
     */
    private static Map getEnumEntryMap(Class enumClass) {
        ClassLoader classLoader = enumClass.getClassLoader();
        Map         entryMap = null;
        synchronized (entries) {
            entryMap = (Map) entries.get(classLoader);

            if (entryMap == null) {
                entryMap = new Hashtable();
                entries.put(classLoader, entryMap);
            }
        }

        return entryMap;
    }

    /**
     * 取得<code>Enum</code>类的<code>EnumType</code>
     *
     * @param enumClass <code>Enum</code>类
     *
     * @return <code>Enum</code>类对应的<code>EnumType</code>对象
     */
    private static EnumType getEnumType(Class enumClass) {
        if (enumClass == null) {
            throw new NullPointerException(EnumConstants.ENUM_CLASS_IS_NULL);
        }

        if (!Enum.class.isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException(MessageFormat.format(EnumConstants.CLASS_IS_NOT_ENUM,
                                                                    new Object[] {
                enumClass.getName()
            }));
        }

        Map      entryMap = getEnumEntryMap(enumClass);
        EnumType enumType = (EnumType) entryMap.get(enumClass.getName());

        if (enumType == null) {
            Method createEnumTypeMethod = findStaticMethod(enumClass,
                                                           EnumConstants.CREATE_ENUM_TYPE_METHOD_NAME,
                                                           new Class[0]);

            if (createEnumTypeMethod != null) {
                try {
                    enumType = (EnumType) createEnumTypeMethod.invoke(null, new Object[0]);
                } catch (IllegalAccessException e) {
                } catch (IllegalArgumentException e) {
                } catch (InvocationTargetException e) {
                } catch (ClassCastException e) {
                }
            }

            if (enumType != null) {
                entryMap.put(enumClass.getName(), enumType);
            }
        }

        if (enumType == null) {
            throw new UnsupportedOperationException(MessageFormat.format(
                                                            EnumConstants.FAILED_CREATING_ENUM_TYPE,
                                                            new Object[] {
                enumClass.getName()
            }));
        }

        return enumType;
    }

    /**
     * 查找方法.
     *
     * @param enumClass   枚举类型
     * @param methodName  方法名
     * @param paramTypes  参数类型表
     *
     * @return 方法对象, 或<code>null</code>表示未找到
     */
    private static Method findStaticMethod(Class enumClass, String methodName, Class[] paramTypes) {
        Method method = null;

        for (Class clazz = enumClass; !clazz.equals(Enum.class); clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, paramTypes);
                break;
            } catch (NoSuchMethodException e) {
            }
        }

        if ((method != null) && Modifier.isStatic(method.getModifiers())) {
            return method;
        }

        return null;
    }

    /**
     * 查找内部类.
     *
     * @param enumClass       枚举类型
     * @param innerClassName  方法名
     * @param superClass      父类
     *
     * @return 内部类对象, 或<code>null</code>表示未找到
     */
    private static Class findStaticInnerClass(Class enumClass, String innerClassName,
                                              Class superClass) {
        innerClassName = enumClass.getName() + "$" + innerClassName;

        Class innerClass = null;

        for (Class clazz = enumClass; !clazz.equals(Enum.class); clazz = clazz.getSuperclass()) {
            Class[] classes = clazz.getDeclaredClasses();

            for (int i = 0; i < classes.length; i++) {
                if (classes[i].getName().equals(innerClassName)
                        && superClass.isAssignableFrom(classes[i])) {
                    innerClass = classes[i];
                    break;
                }
            }
        }

        if ((innerClass != null) && Modifier.isStatic(innerClass.getModifiers())) {
            return innerClass;
        }

        return null;
    }

    /**
     * 取得枚举量的名称.
     *
     * @return 枚举量的名称
     */
    public String getName() {
        return name;
    }

    /**
     * 取得枚举量的值.
     *
     * @return 枚举量的值
     */
    public Object getValue() {
        return value;
    }

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
     * 实现<code>Convertible</code>接口,
     * 取得将当前<code>Enum</code>转换成指定<code>targetType</code>的<code>Converter</code>. 转换的规则如下:
     *
     * <ul>
     * <li>
     * 如果<code>targetType</code>是字符串, 则返回枚举量的名称.
     * </li>
     * <li>
     * 否则将枚举量的值传递到转换链中.
     * </li>
     * </ul>
     *
     *
     * @param targetType 目标类型
     *
     * @return 将当前<code>Enum</code>转换成指定<code>targetType</code>的<code>Converter</code>
     */
    public Converter getConverter(Class targetType) {
        return new Converter() {
            public Object convert(Object value, ConvertChain chain) {
                Enum  enumObj       = (Enum) value;
                Class targetType = chain.getTargetType();

                if (String.class.equals(targetType)) {
                    return enumObj.toString();
                }

                return chain.convert(enumObj.getValue());
            }
        };
    }

    /**
     * 取得和当前枚举量的值相同的位集.
     *
     * @return 新的位集
     */
    public FlagSet createFlagSet() {
        FlagSet flagSet = createFlagSet(getClass());

        if (flagSet != null) {
            return (FlagSet) flagSet.set((Flags) this);
        }

        return null;
    }

    /**
     * 设置成不可变的位集.
     *
     * @return 位集本身
     */
    public Flags setImmutable() {
        return createFlagSet().setImmutable();
    }

    /**
     * 对当前位集执行逻辑与操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    public Flags and(Flags flags) {
        return createFlagSet().and(flags);
    }

    /**
     * 对当前位集执行逻辑非操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    public Flags andNot(Flags flags) {
        return createFlagSet().andNot(flags);
    }

    /**
     * 对当前位集执行逻辑或操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    public Flags or(Flags flags) {
        return createFlagSet().or(flags);
    }

    /**
     * 对当前位集执行逻辑异或操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    public Flags xor(Flags flags) {
        return createFlagSet().xor(flags);
    }

    /**
     * 清除当前位集的全部位.
     *
     * @return 当前位集
     */
    public Flags clear() {
        return createFlagSet().clear();
    }

    /**
     * 清除当前位集的指定位, 等效于<code>andNot</code>操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    public Flags clear(Flags flags) {
        return createFlagSet().clear(flags);
    }

    /**
     * 设置当前位集的指定位, 等效于<code>or</code>操作.
     *
     * @param flags  标志位
     *
     * @return 当前位集
     */
    public Flags set(Flags flags) {
        return createFlagSet().set(flags);
    }

    /**
     * 测试当前位集的指定位, 等效于<code>and(flags) != 0</code>.
     *
     * @param flags  标志位
     *
     * @return 如果指定位被置位, 则返回<code>true</code>
     */
    public boolean test(Flags flags) {
        return createFlagSet().test(flags);
    }

    /**
     * 测试当前位集的指定位, 等效于<code>and(flags) == flags</code>.
     *
     * @param flags  标志位
     *
     * @return 如果指定位被置位, 则返回<code>true</code>
     */
    public boolean testAll(Flags flags) {
        return createFlagSet().test(flags);
    }

    /**
     * 和另一个枚举量比较大小, 就是按枚举量的值比较.
     *
     * @param otherEnum 要比较的枚举量
     *
     * @return 如果等于<code>0</code>, 表示值相等, 大于<code>0</code>表示当前的枚举量的值比<code>otherEnum</code>大,
     *         小于<code>0</code>表示当前的枚举量的值比<code>otherEnum</code>小
     */
    public int compareTo(Object otherEnum) {
        if (!getClass().equals(otherEnum.getClass())) {
            throw new IllegalArgumentException(MessageFormat.format(
                                                       EnumConstants.COMPARE_TYPE_MISMATCH,
                                                       new Object[] {
                getClass().getName(),
                otherEnum.getClass().getName()
            }));
        }

        return ((Comparable) value).compareTo(((Enum) otherEnum).value);
    }

    /**
     * 比较两个枚举量是否相等, 即: 类型相同, 并且值相同(但名字可以不同).
     *
     * @param obj  要比较的对象
     *
     * @return 如果相等, 则返回<code>true</code>
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if ((obj == null) || !getClass().equals(obj.getClass())) {
            return false;
        }

        return value.equals(((Enum) obj).value);
    }

    /**
     * 取得枚举量的hash值.  如果两个枚举量相同, 则它们的hash值一定相同.
     *
     * @return hash值
     */
    public int hashCode() {
        return getClass().hashCode() ^ value.hashCode();
    }

    /**
     * 将枚举量转换成字符串, 也就是枚举量的名称.
     *
     * @return 枚举量的名称
     */
    public String toString() {
        return name;
    }

    /**
     * 被"反序列化"过程调用, 确保返回枚举量的singleton.
     *
     * @return 枚举量的singleton
     *
     * @throws ObjectStreamException 如果反序列化出错
     */
    protected Object readResolve() throws ObjectStreamException {
        Enum enumObj = Enum.getEnumByName(getClass(), getName());

        if ((enumObj == null) || !enumObj.value.equals(value)) {
            throw new InvalidClassException(getClass().getName());
        }

        return enumObj;
    }

    /**
     * 代表一个枚举类型的额外信息.
     */
    protected abstract static class EnumType {
        private Object value;
        final ListMap  nameMap            = new ArrayHashMap();
        final ListMap  valueMap           = new ArrayHashMap();
        boolean        flagSetClassExists = true;
        Class          flagSetClass;
        FlagSet        fullSet;

        /**
         * 设置指定值为当前值.
         *
         * @param value    当前值
         * @param flagMode 是否为位模式
         *
         * @return 当前值
         */
        final Object setValue(Object value, boolean flagMode) {
            this.value = value;

            if (flagMode && !isPowerOfTwo(value)) {
                throw new IllegalArgumentException(MessageFormat.format(
                                                           EnumConstants.VALUE_IS_NOT_POWER_OF_TWO,
                                                           new Object[] {
                    value
                }));
            }

            return value;
        }

        /**
         * 取得下一个值.
         *
         * @param flagMode 是否为位模式
         *
         * @return 当前值
         */
        final Object getNextValue(boolean flagMode) {
            value = getNextValue(value, flagMode);

            if (flagMode && isZero(value)) {
                throw new UnsupportedOperationException(EnumConstants.VALUE_OUT_OF_RANGE);
            }

            return value;
        }

        /**
         * 取得<code>Enum</code>值的类型.
         *
         * @return <code>Enum</code>值的类型
         */
        protected abstract Class getUnderlyingClass();

        /**
         * 取得指定值的下一个值.
         *
         * @param value    指定值
         * @param flagMode 是否为位模式
         *
         * @return 如果<code>value</code>为<code>null</code>, 则返回默认的初始值, 否则返回下一个值
         */
        protected abstract Object getNextValue(Object value, boolean flagMode);

        /**
         * 判断是否为<code>0</code>.
         *
         * @param value 要判断的值
         *
         * @return 如果是, 则返回<code>true</code>
         */
        protected abstract boolean isZero(Object value);

        /**
         * 判断是否为二的整数次幂.
         *
         * @param value 要判断的值
         *
         * @return 如果是, 则返回<code>true</code>
         */
        protected abstract boolean isPowerOfTwo(Object value);
    }
}
