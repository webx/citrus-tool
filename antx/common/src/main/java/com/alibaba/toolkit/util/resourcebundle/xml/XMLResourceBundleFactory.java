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

package com.alibaba.toolkit.util.resourcebundle.xml;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.alibaba.toolkit.util.resourcebundle.AbstractResourceBundleFactory;
import com.alibaba.toolkit.util.resourcebundle.ResourceBundle;
import com.alibaba.toolkit.util.resourcebundle.ResourceBundleConstant;
import com.alibaba.toolkit.util.resourcebundle.ResourceBundleCreateException;
import com.alibaba.toolkit.util.resourcebundle.ResourceBundleLoader;

/**
 * 从XML文件中创建<code>ResourceBundle</code>的实例的工厂.
 * 
 * @version $Id: XMLResourceBundleFactory.java,v 1.1 2003/07/03 07:26:35 baobao
 *          Exp $
 * @author Michael Zhou
 */
public class XMLResourceBundleFactory extends AbstractResourceBundleFactory {
    /**
     * 创建factory, 使用当前线程的context class loader作为bundle装入器.
     */
    public XMLResourceBundleFactory() {
        super();
    }

    /**
     * 创建factory, 使用指定的class loader作为bundle装入器.
     * 
     * @param classLoader 装入bundle的class loader
     */
    public XMLResourceBundleFactory(ClassLoader classLoader) {
        super(classLoader);
    }

    /**
     * 创建factory, 使用指定的loader作为bundle装器
     * 
     * @param loader bundle装入器
     */
    public XMLResourceBundleFactory(ResourceBundleLoader loader) {
        super(loader);
    }

    /**
     * 根据bundle的名称取得resource的文件名称.
     * 
     * @param bundleName bundle的名称
     * @return resource的名称
     */
    @Override
    protected String getFilename(String bundleName) {
        return super.getFilename(bundleName) + ResourceBundleConstant.RB_RESOURCE_EXT_XML;
    }

    /**
     * 以XML格式解析输入流, 并创建<code>ResourceBundle</code>.
     * 
     * @param stream 输入流
     * @param systemId 标志输入流的字符串
     * @return resource bundle
     * @throws ResourceBundleCreateException 如果解析失败
     */
    @Override
    protected ResourceBundle parse(InputStream stream, String systemId) throws ResourceBundleCreateException {
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(stream, systemId);

            return new XMLResourceBundle(doc);
        } catch (DocumentException e) {
            throw new ResourceBundleCreateException(ResourceBundleConstant.RB_FAILED_READING_XML_DOCUMENT,
                    new Object[] { systemId }, e);
        }
    }
}
