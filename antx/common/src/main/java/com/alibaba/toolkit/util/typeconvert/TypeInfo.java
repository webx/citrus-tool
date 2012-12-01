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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.toolkit.util.collection.ArrayHashSet;

/**
 * 代表一个类的信息, 包括父类, 接口, 数组的维数等.
 *
 * @author Michael Zhou
 * @version $Id: TypeInfo.java,v 1.1 2003/07/03 07:26:36 baobao Exp $
 */
public class TypeInfo {
    private static Map typeMap = Collections.synchronizedMap(new HashMap());
    private Class type;
    private Class componentType;
    private int   dimension;
    private List superclasses = new ArrayList(2);
    private List interfaces   = new ArrayList(2);

    /**
     * 取得指定类的<code>TypeInfo</code>.
     *
     * @param type 指定类.
     * @return <code>TypeInfo</code>对象.
     */
    public static TypeInfo getTypeInfo(Class type) {
        if (type == null) {
            throw new NullPointerException();
        }

        TypeInfo typeInfo;
        synchronized (typeMap) {
            typeInfo = (TypeInfo) typeMap.get(type);

            if (typeInfo == null) {
                typeInfo = new TypeInfo(type);
                typeMap.put(type, typeInfo);
            }
        }

        return typeInfo;
    }

    /**
     * 创建<code>TypeInfo</code>.
     *
     * @param type 创建指定类的<code>TypeInfo</code>
     */
    private TypeInfo(Class type) {
        this.type = type;

        // 如果是array, 设置componentType和dimension
        if (type.isArray()) {
            this.componentType = type;

            do {
                componentType = componentType.getComponentType();
                dimension++;
            } while (componentType.isArray());
        }

        // 取得所有superclass
        if (dimension > 0) {
            Class superComponentType = componentType.getSuperclass();

            // 如果是primitive, interface, 则设置其基类为Object.
            if (superComponentType == null && !Object.class.equals(componentType)) {
                superComponentType = Object.class;
            }

            if (superComponentType != null) {
                Class superclass = getArrayClass(superComponentType, dimension);

                superclasses.add(superclass);
                superclasses.addAll(getTypeInfo(superclass).superclasses);
            } else {
                for (int i = dimension - 1; i >= 0; i--) {
                    superclasses.add(getArrayClass(Object.class, i));
                }
            }
        } else {
            Class superclass = type.getSuperclass();

            if (superclass != null) {
                superclasses.add(superclass);
                superclasses.addAll(getTypeInfo(superclass).superclasses);
            }
        }

        // 取得所有interface
        if (dimension == 0) {
            Class[] typeInterfaces = type.getInterfaces();
            Set set = new ArrayHashSet();

            for (Class typeInterface : typeInterfaces) {
                set.add(typeInterface);
                set.addAll(getTypeInfo(typeInterface).interfaces);
            }

            for (Iterator i = superclasses.iterator(); i.hasNext(); ) {
                Class typeInterface = (Class) i.next();

                set.addAll(getTypeInfo(typeInterface).interfaces);
            }

            interfaces.addAll(set);
        }
    }

    /**
     * 取得<code>TypeInfo</code>所代表的java类.
     *
     * @return <code>TypeInfo</code>所代表的java类
     */
    public Class getType() {
        return type;
    }

    /**
     * 取得数组元素的类型.
     *
     * @return 如果是数组, 则返回数组元素的类型, 否则返回<code>null</code>
     */
    public Class getComponentType() {
        return componentType;
    }

    /**
     * 取得数组的维数.
     *
     * @return 数组的维数. 如果不是数组, 则返回<code>0</code>
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * 取得所有的父类.
     *
     * @return 所有的父类
     */
    public List getSuperclasses() {
        return Collections.unmodifiableList(superclasses);
    }

    /**
     * 取得所有的接口.
     *
     * @return 所有的接口
     */
    public List getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    /**
     * 取得指定维数的<code>Array</code>类.
     *
     * @param componentType 数组的基类
     * @param dimension     维数
     * @return 如果维数为0, 则返回基类本身, 否则返回数组类
     */
    public static Class getArrayClass(Class componentType, int dimension) {
        if (dimension == 0) {
            return componentType;
        }

        return Array.newInstance(componentType, new int[dimension]).getClass();
    }
}
