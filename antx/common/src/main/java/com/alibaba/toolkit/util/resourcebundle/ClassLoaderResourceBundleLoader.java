/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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

package com.alibaba.toolkit.util.resourcebundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import com.alibaba.toolkit.util.ContextClassLoader;

/**
 * 通过<code>ClassLoader</code>装入resource bundle的数据.
 *
 * @author Michael Zhou
 * @version $Id: ClassLoaderResourceBundleLoader.java,v 1.1 2003/07/03 07:26:34
 *          baobao Exp $
 */
public class ClassLoaderResourceBundleLoader implements ResourceBundleLoader {
    private ClassLoader classLoader;

    /** 创建新loader, 使用当前线程的context class loader. */
    public ClassLoaderResourceBundleLoader() {
        this(null);
    }

    /**
     * 创建新loader, 使用指定的class loader.
     *
     * @param classLoader 指定的class loader
     */
    public ClassLoaderResourceBundleLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            this.classLoader = ContextClassLoader.getClassLoader();

            if (this.classLoader == null) {
                this.classLoader = ClassLoader.getSystemClassLoader();
            }
        } else {
            this.classLoader = classLoader;
        }
    }

    /**
     * 根据指定的bundle名称, 取得输入流.
     *
     * @param bundleFilename 要查找的bundle名
     * @return bundle的数据流, 如果指定bundle文件不存在, 则返回<code>null</code>
     * @throws ResourceBundleCreateException 如果文件存在, 但读取数据流失败
     */
    public InputStream openStream(final String bundleFilename) throws ResourceBundleCreateException {
        try {
            return (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ResourceBundleCreateException {
                    URL url = classLoader.getResource(bundleFilename);

                    // 如果资源不存在, 则返回null
                    if (url == null) {
                        return null;
                    }

                    try {
                        return url.openStream();
                    } catch (IOException e) {
                        throw new ResourceBundleCreateException(ResourceBundleConstant.RB_FAILED_OPENING_STREAM,
                                                                new Object[] { bundleFilename }, e);
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            throw (ResourceBundleCreateException) e.getException();
        }
    }

    /**
     * 判断两个<code>ResourceBundleLoader</code>是否等效. 这将作为
     * <code>ResourceBundle</code>的cache的依据. 具有相同的context class loader的
     * <code>ResourceBundleLoader</code>是等效的.
     *
     * @param obj 要比较的另一个对象
     * @return 如果等效, 则返回<code>true</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ClassLoaderResourceBundleLoader)) {
            return false;
        }

        return ((ClassLoaderResourceBundleLoader) obj).classLoader == classLoader;
    }

    /**
     * 取得hash值. 等效的<code>ResourceBundleLoader</code>应该具有相同的hash值.
     *
     * @return hash值
     */
    @Override
    public int hashCode() {
        return classLoader == null ? 0 : classLoader.hashCode();
    }
}
