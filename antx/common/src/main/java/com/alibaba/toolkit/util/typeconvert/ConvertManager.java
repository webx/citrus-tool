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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.alibaba.toolkit.util.enumeration.Enum;
import com.alibaba.toolkit.util.enumeration.EnumConverter;
import com.alibaba.toolkit.util.enumeration.FlagSet;
import com.alibaba.toolkit.util.enumeration.FlagSetConverter;
import com.alibaba.toolkit.util.typeconvert.converters.BigDecimalConverter;
import com.alibaba.toolkit.util.typeconvert.converters.BigIntegerConverter;
import com.alibaba.toolkit.util.typeconvert.converters.BooleanConverter;
import com.alibaba.toolkit.util.typeconvert.converters.ByteConverter;
import com.alibaba.toolkit.util.typeconvert.converters.CharacterConverter;
import com.alibaba.toolkit.util.typeconvert.converters.DoubleConverter;
import com.alibaba.toolkit.util.typeconvert.converters.FloatConverter;
import com.alibaba.toolkit.util.typeconvert.converters.IntegerConverter;
import com.alibaba.toolkit.util.typeconvert.converters.LongConverter;
import com.alibaba.toolkit.util.typeconvert.converters.ObjectArrayConverter;
import com.alibaba.toolkit.util.typeconvert.converters.ObjectConverter;
import com.alibaba.toolkit.util.typeconvert.converters.ShortConverter;
import com.alibaba.toolkit.util.typeconvert.converters.SqlDateConverter;
import com.alibaba.toolkit.util.typeconvert.converters.SqlTimeConverter;
import com.alibaba.toolkit.util.typeconvert.converters.SqlTimestampConverter;
import com.alibaba.toolkit.util.typeconvert.converters.StringConverter;

/**
 * <code>Converter</code>的管理器.
 *
 * @author Michael Zhou
 * @version $Id: ConvertManager.java,v 1.1 2003/07/03 07:26:36 baobao Exp $
 */
public class ConvertManager {
    private static final Object NO_DEFAULT_VALUE = new Object();
    private              Map    registry         = Collections.synchronizedMap(new HashMap());
    private              Map    aliases          = Collections.synchronizedMap(new HashMap());

    /** 创建一个转换器. */
    public ConvertManager() {
        register(BigDecimal.class, new BigDecimalConverter());
        register(BigInteger.class, new BigIntegerConverter());
        register(Boolean.class, new BooleanConverter());
        register(Byte.class, new ByteConverter());
        register(Character.class, new CharacterConverter());
        register(Double.class, new DoubleConverter());
        register(Float.class, new FloatConverter());
        register(Integer.class, new IntegerConverter());
        register(Long.class, new LongConverter());
        register(Short.class, new ShortConverter());
        register(Date.class, new SqlDateConverter());
        register(Time.class, new SqlTimeConverter());
        register(Timestamp.class, new SqlTimestampConverter());
        register(String.class, new StringConverter());
        register(Object[].class, new ObjectArrayConverter());
        register(Object.class, new ObjectConverter());
        register(Enum.class, new EnumConverter());
        register(FlagSet.class, new FlagSetConverter());

        // 登记别名.
        registerAlias("string", String.class);
        registerAlias("enum", Enum.class);
    }

    /**
     * 登录一个转换器.
     *
     * @param type      转换器的目标类型
     * @param converter 转换器对象
     */
    public void register(Class type, Converter converter) {
        synchronized (registry) {
            if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
                internalRegister(Boolean.class, converter);
                internalRegister(Boolean.TYPE, converter);
            } else if (Byte.class.equals(type) || Byte.TYPE.equals(type)) {
                internalRegister(Byte.class, converter);
                internalRegister(Byte.TYPE, converter);
            } else if (Character.class.equals(type) || Character.TYPE.equals(type)) {
                internalRegister(Character.class, converter);
                internalRegister(Character.TYPE, converter);
            } else if (Double.class.equals(type) || Double.TYPE.equals(type)) {
                internalRegister(Double.class, converter);
                internalRegister(Double.TYPE, converter);
            } else if (Float.class.equals(type) || Float.TYPE.equals(type)) {
                internalRegister(Float.class, converter);
                internalRegister(Float.TYPE, converter);
            } else if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
                internalRegister(Integer.class, converter);
                internalRegister(Integer.TYPE, converter);
            } else if (Long.class.equals(type) || Long.TYPE.equals(type)) {
                internalRegister(Long.class, converter);
                internalRegister(Long.TYPE, converter);
            } else if (Short.class.equals(type) || Short.TYPE.equals(type)) {
                internalRegister(Short.class, converter);
                internalRegister(Short.TYPE, converter);
            } else {
                internalRegister(type, converter);
            }
        }
    }

    /**
     * 登记别名.
     *
     * @param alias 别名
     * @param type  目标类型
     */
    public void registerAlias(String alias, Class type) {
        synchronized (aliases) {
            if (alias != null && type != null && !aliases.containsKey(alias)) {
                aliases.put(alias, type);
            }
        }
    }

    /**
     * 内部过程: 登记一个转换器. 不检查primitive类型, 不同步.
     *
     * @param type      转换器的目标类型
     * @param converter 转换器对象
     */
    private void internalRegister(Class type, Converter converter) {
        LinkedList converters = (LinkedList) registry.get(type);

        if (converters == null) {
            converters = new LinkedList();
            registry.put(type, converters);
        }

        if (!converters.contains(converter)) {
            converters.addFirst(converter);
            registerAlias(type.getName(), type);
        }
    }

    /**
     * 将指定值转换成<code>boolean</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public boolean asBoolean(Object value) {
        return ((Boolean) asType(Boolean.class, value)).booleanValue();
    }

    /**
     * 将指定值转换成<code>boolean</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public boolean asBoolean(Object value, boolean defaultValue) {
        return ((Boolean) asType(Boolean.class, value, new Boolean(defaultValue))).booleanValue();
    }

    /**
     * 将指定值转换成<code>byte</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public byte asByte(Object value) {
        return ((Byte) asType(Byte.class, value)).byteValue();
    }

    /**
     * 将指定值转换成<code>byte</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public byte asByte(Object value, byte defaultValue) {
        return ((Byte) asType(Byte.class, value, new Byte(defaultValue))).byteValue();
    }

    /**
     * 将指定值转换成<code>char</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public char asChar(Object value) {
        return ((Character) asType(Character.class, value)).charValue();
    }

    /**
     * 将指定值转换成<code>char</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public char asChar(Object value, char defaultValue) {
        return ((Character) asType(Character.class, value, new Character(defaultValue))).charValue();
    }

    /**
     * 将指定值转换成<code>double</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public double asDouble(Object value) {
        return ((Double) asType(Double.class, value)).doubleValue();
    }

    /**
     * 将指定值转换成<code>double</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public double asDouble(Object value, double defaultValue) {
        return ((Double) asType(Double.class, value, new Double(defaultValue))).doubleValue();
    }

    /**
     * 将指定值转换成<code>float</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public float asFloat(Object value) {
        return ((Float) asType(Float.class, value)).floatValue();
    }

    /**
     * 将指定值转换成<code>float</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public float asFloat(Object value, float defaultValue) {
        return ((Float) asType(Float.class, value, new Float(defaultValue))).floatValue();
    }

    /**
     * 将指定值转换成<code>int</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public int asInt(Object value) {
        return ((Integer) asType(Integer.class, value)).intValue();
    }

    /**
     * 将指定值转换成<code>int</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public int asInt(Object value, int defaultValue) {
        return ((Integer) asType(Integer.class, value, new Integer(defaultValue))).intValue();
    }

    /**
     * 将指定值转换成<code>long</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public long asLong(Object value) {
        return ((Long) asType(Long.class, value)).longValue();
    }

    /**
     * 将指定值转换成<code>long</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public long asLong(Object value, long defaultValue) {
        return ((Long) asType(Long.class, value, new Long(defaultValue))).longValue();
    }

    /**
     * 将指定值转换成<code>short</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public short asShort(Object value) {
        return ((Short) asType(Short.class, value)).shortValue();
    }

    /**
     * 将指定值转换成<code>short</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public short asShort(Object value, short defaultValue) {
        return ((Short) asType(Short.class, value, new Short(defaultValue))).shortValue();
    }

    /**
     * 将指定值转换成<code>String</code>类型.
     *
     * @param value 要转换的值
     * @return 转换后的值
     */
    public String asString(Object value) {
        return (String) asType(String.class, value);
    }

    /**
     * 将指定值转换成<code>String</code>类型.
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public String asString(Object value, String defaultValue) {
        return (String) asType(String.class, value, defaultValue);
    }

    /**
     * 将指定值转换成指定类型.
     *
     * @param targetType 要转换的目标类型
     * @param value      要转换的值
     * @return 转换后的值
     */
    public Object asType(Object targetType, Object value) {
        return asType(targetType, value, NO_DEFAULT_VALUE);
    }

    /**
     * 将指定值转换成指定类型.
     *
     * @param targetType   要转换的目标类型
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public Object asType(Object targetType, Object value, Object defaultValue) {
        try {
            return new ChainImpl(this, getTargetType(targetType)).convert(value);
        } catch (ConvertFailedException e) {
            if (e.isDefaultValueSet()) {
                return defaultValue == NO_DEFAULT_VALUE ? e.getDefaultValue() : defaultValue;
            }

            throw e;
        }
    }

    /**
     * 将指定值转换成指定类型. 即使转换失败, 也不会返回默认值, 而抛出一个异常.
     *
     * @param targetType 要转换的目标类型
     * @param value      要转换的值
     * @return 转换后的值
     */
    public Object asTypeWithoutDefaultValue(Object targetType, Object value) {
        return new ChainImpl(this, getTargetType(targetType)).convert(value);
    }

    /**
     * 取得target type类对象.
     *
     * @param targetType target type类或别名
     * @return target type类对象
     */
    private Class getTargetType(Object targetType) {
        if (targetType instanceof Class) {
            return (Class) targetType;
        }

        if (targetType instanceof String) {
            Class type = (Class) aliases.get(targetType);

            if (type != null) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid targetType " + targetType);
    }

    /**
     * 转换器链. 依次尝试: Convertible.getConverter(targetType), targetType对应的转换器,
     * targetType的基类(不包括Object类)对应的转换器, targetType的接口对应的转换器, Object类所对应的转换器.
     */
    private class ChainImpl implements ConvertChain {
        private static final int STATE_START       = 0;
        private static final int STATE_TARGET_TYPE = STATE_START + 1;
        private static final int STATE_BASE_TYPE   = STATE_TARGET_TYPE + 1;
        private static final int STATE_INTERFACE   = STATE_BASE_TYPE + 1;
        private static final int STATE_OBJECT      = STATE_INTERFACE + 1;
        private static final int STATE_END         = STATE_OBJECT + 1;
        private final ConvertManager manager;
        private final TypeInfo       targetTypeInfo;
        private int state = STATE_START;
        private Convertible previousConvertibleValue;
        private Iterator    converterIterator;
        private Iterator    superclassIterator;
        private Iterator    interfaceIterator;

        /**
         * 创建转换链.
         *
         * @param manager    创建此链的<code>ConvertManager</code>
         * @param targetType 转换的目标类型
         */
        ChainImpl(ConvertManager manager, Class targetType) {
            this.manager = manager;
            this.targetTypeInfo = TypeInfo.getTypeInfo(targetType);
        }

        /**
         * 取得创建此链的<code>ConvertManager</code>.
         *
         * @return 创建此链的<code>ConvertManager</code>
         */
        public ConvertManager getConvertManager() {
            return manager;
        }

        /**
         * 取得转换的目标类型.
         *
         * @return 目标类型
         */
        public Class getTargetType() {
            return targetTypeInfo.getType();
        }

        /**
         * 将控制交给链中的下一个转换器, 转换指定的值到指定的类型.
         *
         * @param value 要转换的值
         * @return 转换后的值
         */
        public Object convert(Object value) {
            Class targetType = targetTypeInfo.getType();

            // 优先处理实现Convertible接口的value值,
            // 并防止对同一个convertible value反复调用其converter
            if (value instanceof Convertible && !value.equals(previousConvertibleValue)) {
                Converter converter = ((Convertible) value).getConverter(targetType);

                if (converter != null) {
                    previousConvertibleValue = (Convertible) value;
                    return converter.convert(value, this);
                }
            }

            // 开始状态
            if (state == STATE_START) {
                state++; // 进入STATE_TARGET_TYPE状态
                converterIterator = getConverterIterator(targetType);
            }

            // 处理targetType对应的转换器
            if (state == STATE_TARGET_TYPE) {
                if (!hasNext(converterIterator)) {
                    state++; // 进入STATE_BASE_TYPE状态
                    superclassIterator = targetTypeInfo.getSuperclasses().iterator();
                    converterIterator = getSuperclassConverterIterator();
                }
            }

            // 处理targetType的基类对应的转换器
            if (state == STATE_BASE_TYPE) {
                if (!hasNext(converterIterator)) {
                    converterIterator = getSuperclassConverterIterator();

                    if (!hasNext(converterIterator)) {
                        state++; // 进入STATE_INTERFACE状态
                        interfaceIterator = targetTypeInfo.getInterfaces().iterator();
                        converterIterator = getInterfaceConverterIterator();
                    }
                }
            }

            // 处理targetType的接口对应的转换器
            if (state == STATE_INTERFACE) {
                if (!hasNext(converterIterator)) {
                    converterIterator = getInterfaceConverterIterator();

                    if (!hasNext(converterIterator)) {
                        state++; // 进入STATE_OBJECT状态

                        if (!Object.class.equals(targetTypeInfo.getType())) {
                            converterIterator = getConverterIterator(Object.class);
                        }
                    }
                }
            }

            // 处理Object类对应的转换器
            if (state == STATE_OBJECT) {
                if (!hasNext(converterIterator)) {
                    state++; // 进入STATE_END状态
                }
            }

            // 开始转换
            if (state != STATE_END) {
                Converter converter = (Converter) converterIterator.next();

                return converter.convert(value, this);
            }

            // 如果找不到converter, 则失败
            throw new ConvertFailedException();
        }

        /**
         * 取得指定类型的所有转换器.
         *
         * @param type 要查找转换器的类型
         * @return 转换器的<code>Iterator</code>, 如果不存在, 则返回<code>null</code>
         */
        private Iterator getConverterIterator(Class type) {
            LinkedList converters = (LinkedList) registry.get(type);

            if (converters != null && converters.size() > 0) {
                return converters.iterator();
            }

            return null;
        }

        /**
         * 取得targetType基类(不包括Object类)的转换器.
         *
         * @return 基类的转换器的<code>Iterator</code>, 如果不存在, 则返回<code>null</code>
         */
        private Iterator getSuperclassConverterIterator() {
            Iterator iterator = null;

            while (superclassIterator.hasNext()) {
                Class superclass = (Class) superclassIterator.next();

                if (!superclass.equals(Object.class)) {
                    iterator = getConverterIterator(superclass);

                    if (hasNext(iterator)) {
                        return iterator;
                    }
                }
            }

            return null;
        }

        /**
         * 取得targetType接口的转换器.
         *
         * @return 接口的转换器的<code>Iterator</code>, 如果不存在, 则返回<code>null</code>
         */
        private Iterator getInterfaceConverterIterator() {
            Iterator iterator = null;

            while (interfaceIterator.hasNext()) {
                iterator = getConverterIterator((Class) interfaceIterator.next());

                if (hasNext(iterator)) {
                    return iterator;
                }
            }

            return null;
        }

        /**
         * 检查iterator是否存在下一个元素.
         *
         * @param iterator 要检查的iterator
         * @return 如果iterator为<code>null</code>, 或iterator已经走到底, 则返回
         *         <code>false</code>
         */
        private boolean hasNext(Iterator iterator) {
            return iterator != null && iterator.hasNext();
        }
    }
}
