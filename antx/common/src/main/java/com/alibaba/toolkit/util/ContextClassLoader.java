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
package com.alibaba.toolkit.util;

import com.alibaba.toolkit.util.collection.ArrayHashSet;

import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Method;

import java.net.URL;

import java.util.Enumeration;
import java.util.Set;

/**
 * <p>
 * 查找并装入类和资源的辅助类.
 * </p>
 *
 * <p>
 * <code>ClassFinder</code>查找类和资源的效果,
 * 相当于<code>ClassLoader.loadClass</code>方法和<code>ClassLoader.getResource</code>方法.
 * 但<code>ClassFinder</code>总是首先尝试从<code>Thread.getContextClassLoader()</code>方法取得<code>ClassLoader</code>中并装入类和资源.
 * 这种方法避免了在多级<code>ClassLoader</code>的情况下, 找不到类或资源的情况.
 * </p>
 *
 * <p>
 * 假设有如下情况:
 * </p>
 *
 * <ul>
 * <li>
 * 工具类<code>A</code>是从系统<code>ClassLoader</code>装入的(classpath)
 * </li>
 * <li>
 * 类<code>B</code>是Web Application中的一个类, 是由servlet引擎的<code>ClassLoader</code>动态装入的
 * </li>
 * <li>
 * 资源文件<code>C.properties</code>也在Web Application中, 只有servlet引擎的动态<code>ClassLoader</code>可以找到它
 * </li>
 * <li>
 * 类<code>B</code>调用工具类<code>A</code>的方法, 希望通过类<code>A</code>取得资源文件<code>C.properties</code>
 * </li>
 * </ul>
 *
 * <p>
 * 如果类<code>A</code>使用<code>getClass().getClassLoader().getResource(&quot;C.properties&quot;)</code>,
 * 就会失败, 因为系统<code>ClassLoader</code>不能找到此资源.
 * 但类A可以使用ClassFinder.getResource(&quot;C.properties&quot;), 就可以找到这个资源,
 * 因为ClassFinder调用<code>Thread.currentThead().getContextClassLoader()</code>取得了servlet引擎的<code>ClassLoader</code>,
 * 从而找到了这个资源文件.
 * </p>
 *
 * <p>
 * 注意, <code>Thread.getContextClassLoader()</code>是在JDK1.2之后才有的, 对于低版本的JDK,
 * <code>ClassFinder</code>的效果和直接调用<code>ClassLoader</code>完全相同.
 * </p>
 *
 * @version $Id: ContextClassLoader.java,v 1.1 2003/07/03 07:26:15 baobao Exp $
 * @author Michael Zhou
 */
public class ContextClassLoader {
    /**
     * JDK1.2以上, 这个变量保存了<code>Thread.getContextClassLoader()</code>方法. 对于低版本的JDK,
     * 此变量为<code>null</code>.
     */
    private static Method GET_CONTEXT_CLASS_LOADER_METHOD = null;

    static {
        try {
            GET_CONTEXT_CLASS_LOADER_METHOD = Thread.class.getMethod("getContextClassLoader", null);
        } catch (NoSuchMethodException e) {
            // JDK 1.2以下.
        }
    }

    /**
     * <p>
     * 从<code>ClassLoader</code>取得所有resource URL. 按如下顺序查找:
     * </p>
     *
     * <ol>
     * <li>
     * 在当前线程的<code>ClassLoader</code>中查找.
     * </li>
     * <li>
     * 在装入自己的<code>ClassLoader</code>中查找.
     * </li>
     * <li>
     * 通过<code>ClassLoader.getSystemResource</code>方法查找.
     * </li>
     * </ol>
     *
     *
     * @param resourceName 要查找的资源名, 就是以&quot;/&quot;分隔的标识符字符串
     *
     * @return resource的URL数组, 如果没找到, 则返回空数组. 数组中保证不包含重复的URL.
     */
    public static URL[] getResources(String resourceName) {
        ClassLoader classLoader = null;
        Set         urlSet = new ArrayHashSet();
        boolean     found  = false;


        // 首先试着从当前线程的ClassLoader中查找.
        found = getResources(urlSet, resourceName, getClassLoader(), false);

        // 如果没找到, 试着从装入自己的ClassLoader中查找.
        if (!found) {
            getResources(urlSet, resourceName, ContextClassLoader.class.getClassLoader(), false);
        }

        // 最后的尝试: 在系统ClassLoader中查找(JDK1.2以上),
        // 或者在JDK的内部ClassLoader中查找(JDK1.2以下).
        if (!found) {
            getResources(urlSet, resourceName, null, true);
        }

        if (found) {
            return (URL[]) urlSet.toArray(new URL[urlSet.size()]);
        }

        return new URL[0];
    }

    /**
     * 在指定class loader中查找指定名称的resource, 把所有找到的resource的URL放入指定的集合中.
     *
     * @param urlSet          存放resource URL的集合
     * @param resourceName    资源名
     * @param classLoader     类装入器
     * @param sysClassLoader  是否用system class loader装载资源
     *
     * @return 如果找到, 则返回<code>true</code>
     */
    private static boolean getResources(Set urlSet, String resourceName, ClassLoader classLoader,
                                        boolean sysClassLoader) {
        Enumeration i = null;

        try {
            if (classLoader != null) {
                i = classLoader.getResources(resourceName);
            } else if (sysClassLoader) {
                i = ClassLoader.getSystemResources(resourceName);
            }
        } catch (IOException e) {
        }

        if ((i != null) && i.hasMoreElements()) {
            while (i.hasMoreElements()) {
                urlSet.add(i.nextElement());
            }

            return true;
        }

        return false;
    }

    /**
     * <p>
     * 从<code>ClassLoader</code>取得resource URL. 按如下顺序查找:
     * </p>
     *
     * <ol>
     * <li>
     * 在当前线程的<code>ClassLoader</code>中查找.
     * </li>
     * <li>
     * 在装入自己的<code>ClassLoader</code>中查找.
     * </li>
     * <li>
     * 通过<code>ClassLoader.getSystemResource</code>方法查找.
     * </li>
     * </ol>
     *
     *
     * @param resourceName 要查找的资源名, 就是以&quot;/&quot;分隔的标识符字符串
     *
     * @return resource的URL
     */
    public static URL getResource(String resourceName) {
        ClassLoader classLoader = null;
        URL         url = null;


        // 首先试着从当前线程的ClassLoader中查找.
        classLoader = getClassLoader();

        if (classLoader != null) {
            url = classLoader.getResource(resourceName);

            if (url != null) {
                return url;
            }
        }


        // 如果没找到, 试着从装入自己的ClassLoader中查找.
        classLoader = ContextClassLoader.class.getClassLoader();

        if (classLoader != null) {
            url = classLoader.getResource(resourceName);

            if (url != null) {
                return url;
            }
        }

        // 最后的尝试: 在系统ClassLoader中查找(JDK1.2以上),
        // 或者在JDK的内部ClassLoader中查找(JDK1.2以下).
        return ClassLoader.getSystemResource(resourceName);
    }

    /**
     * 从<code>ClassLoader</code>取得resource的输入流.
     * 相当于<code>getResource(resourceName).openStream()</code>.
     *
     * @param resourceName 要查找的资源名, 就是以"/"分隔的标识符字符串
     *
     * @return resource的输入流
     */
    public static InputStream getResourceAsStream(String resourceName) {
        URL url = getResource(resourceName);

        try {
            if (url != null) {
                return url.openStream();
            }
        } catch (IOException e) {
            // 打开URL失败.
        }

        return null;
    }

    /**
     * 从当前线程的<code>ClassLoader</code>装入类.  对于JDK1.2以下, 则相当于<code>Class.forName</code>.
     *
     * @param className 要装入的类名
     *
     * @return 已装入的类
     *
     * @throws ClassNotFoundException 如果类没找到
     */
    public static Class loadClass(String className)
            throws ClassNotFoundException {
        return loadClass(className, true, null);
    }

    /**
     * 从指定的<code>ClassLoader</code>中装入类.  如果未指定<code>ClassLoader</code>,
     * 则从当前线程的<code>ClassLoader</code>中装入.
     *
     * @param className   要装入的类名
     * @param initialize  是否要初始化类
     * @param classLoader 从指定的<code>ClassLoader</code>中装入类
     *
     * @return 已装入的类
     *
     * @throws ClassNotFoundException 如果类没找到
     */
    public static Class loadClass(String className, boolean initialize, ClassLoader classLoader)
            throws ClassNotFoundException {
        if (classLoader == null) {
            classLoader = getClassLoader();
        }

        return Class.forName(className, initialize, classLoader);
    }

    /**
     * 取得当前线程的<code>ClassLoader</code>. 这个功能需要JDK1.2或更高版本的JDK的支持.
     *
     * @return 如果JDK是1.2以前版本, 返回null. 否则返回当前线程的<code>ClassLoader</code>.
     */
    public static ClassLoader getClassLoader() {
        if (GET_CONTEXT_CLASS_LOADER_METHOD != null) {
            try {
                return (ClassLoader) GET_CONTEXT_CLASS_LOADER_METHOD.invoke(Thread.currentThread(),
                                                                            null);
            } catch (Throwable e) {
                return null;
            }
        }

        return null;
    }
}
