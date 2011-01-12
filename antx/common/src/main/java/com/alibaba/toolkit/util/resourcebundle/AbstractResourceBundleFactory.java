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
package com.alibaba.toolkit.util.resourcebundle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 抽象的<code>ResourceBundleFactory</code>, 实现了通用的创建<code>ResourceBundle</code>实例的方法.
 *
 * <p>
 * 这个实现通过<code>ResourceBundleLoader</code>装入bundle的数据文件,
 * 然后执行<code>parse</code>方法取得<code>ResourceBundle</code>实例.
 * </p>
 *
 * <p>
 * 扩展类可以通过提供适当的<code>ResourceBundleLoader</code>来改变bundle的装入方式, 例如从文件系统或数据库中装入.
 * 通过覆盖<code>parse</code>方法, 可以改变读取文件的格式, 例如<code>XMLResourceBundleFactory</code>以XML的格式来解释文件.
 * </p>
 *
 * @version $Id
 * @author Michael Zhou
 */
public abstract class AbstractResourceBundleFactory
        extends ResourceBundleFactory {
    private final ResourceBundleLoader loader;

    /**
     * 创建factory, 使用当前线程的context class loader作为bundle装入器.
     */
    public AbstractResourceBundleFactory() {
        this(new ClassLoaderResourceBundleLoader());
    }

    /**
     * 创建factory, 使用指定的class loader作为bundle装入器.
     *
     * @param classLoader 装入bundle的class loader
     */
    public AbstractResourceBundleFactory(ClassLoader classLoader) {
        this(new ClassLoaderResourceBundleLoader(classLoader));
    }

    /**
     * 创建factory, 使用指定的loader作为bundle装器
     *
     * @param loader bundle装入器
     */
    public AbstractResourceBundleFactory(ResourceBundleLoader loader) {
        this.loader = loader;
    }

    /**
     * 取得<code>ResourceBundleLoader</code>.
     *
     * @return loader
     */
    public ResourceBundleLoader getLoader() {
        return loader;
    }

    /**
     * 创建<code>ResourceBundle</code>的实例.
     *
     * @param bundleName  要创建的bundle名称
     *
     * @return 新创建的<code>ResourceBundle</code>实例, 如果指定bundle不存在, 则返回<code>null</code>
     *
     * @throws ResourceBundleCreateException 指定bundle文件存在, 但创建bundle实例失败, 例如文件格式错误
     */
    public ResourceBundle createBundle(String bundleName)
            throws ResourceBundleCreateException {
        InputStream stream   = null;
        String      filename = getFilename(bundleName);

        if (loader != null) {
            stream = loader.openStream(filename);
        }

        // 如果文件不存在, 则返回null.
        if (stream == null) {
            return null;
        }

        try {
            
            // @TODO: 此处最好以URL作为system ID
            return parse(new BufferedInputStream(stream), filename);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 根据bundle的名称取得resource的文件名称.
     *
     * @param bundleName  bundle的名称
     *
     * @return resource的名称
     */
    protected String getFilename(String bundleName) {
        return bundleName.replace('.', '/');
    }

    /**
     * 解析输入流, 从中创建<code>ResourceBundle</code>.
     *
     * @param stream    输入流
     * @param systemId  标准输入流的字符串(一般是文件名)
     *
     * @return resource bundle
     *
     * @throws ResourceBundleCreateException 如果解析失败
     */
    protected abstract ResourceBundle parse(InputStream stream, String systemId)
            throws ResourceBundleCreateException;

    /**
     * 比较两个factory是否等效.  对于等效的factory, 给予相同的bundle名, 调用<code>createBundle</code>方法,
     * 可以得到等效的bundle实例.
     *
     * @param other 要比较的factory
     *
     * @return 如果等效, 则返回<code>true</code>
     */
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        try {
            AbstractResourceBundleFactory otherFactory = (AbstractResourceBundleFactory) other;

            return (loader == null)
                       ? (otherFactory.loader == null)
                       : loader.equals(otherFactory.loader);
        } catch (NullPointerException npe) {
            return false;
        } catch (ClassCastException cce) {
            return false;
        }
    }

    /**
     * 取得factory的hash值, 如果两个factory等效, 则它们的hash值也相等.
     *
     * @return factory的hash值
     */
    public int hashCode() {
        return (loader == null) ? 0
                                : loader.hashCode();
    }
}
