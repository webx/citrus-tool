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

package com.alibaba.antx.config.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.config.generator.expr.CompositeExpression;
import com.alibaba.antx.config.generator.expr.Expression;
import com.alibaba.antx.config.generator.expr.ExpressionContext;
import com.alibaba.antx.config.generator.expr.ReferenceExpression;
import com.alibaba.antx.util.StringUtil;
import com.alibaba.antx.util.collection.ExtendedProperties;

public abstract class PropertiesLoader {
    /**
     * 装入属性文件。
     * 
     * @param propsFile 属性文件
     * @param propsCharset 装入属性文件时使用的编码字符集
     * @return 属性文件的内容
     */
    public static Map loadPropertiesFile(InputStream istream, String propsCharset, String url) {
        return loadPropertiesFile(istream, propsCharset, url, true);
    }

    /**
     * 装入属性文件。
     * 
     * @param propsFile 属性文件
     * @param propsCharset 装入属性文件时使用的编码字符集
     * @return 属性文件的内容
     */
    public static Map loadPropertiesFile(InputStream istream, String propsCharset, String url, boolean closeOnExit) {
        ExtendedProperties props = new ExtendedProperties();

        try {
            props.load(istream, propsCharset, url);
        } catch (IOException e) {
            throw new ConfigException(e);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException e) {
                }
            }
        }

        return props;
    }

    /**
     * 装入属性文件。
     * 
     * @param propsFile 属性文件
     * @param propsCharset 装入属性文件时使用的编码字符集
     * @return 属性文件的内容
     */
    public static Map loadPropertiesFile(File propsFile, String propsCharset) {
        ExtendedProperties props = new ExtendedProperties();

        if (propsFile.exists()) {
            try {
                props.load(propsFile.toURI().toURL(), propsCharset);
            } catch (IOException e) {
                throw new ConfigException(e);
            }
        }

        return props;
    }

    /**
     * 将属性从源属性表中合并到目标属性表，如果有重名的，则覆盖之。
     * 
     * @param dest 目标属性表
     * @param src 源属性表
     */
    public static void mergeProperties(Map dest, Map src) {
        for (Iterator i = src.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                Expression expr = CompositeExpression.parse((String) value);

                if (expr != null) {
                    value = expr;
                }
            }

            dest.put(name, value);
            dest.put(StringUtil.getValidIdentifier(name), value);
        }
    }

    /**
     * 取值并计算。
     */
    public static Object evaluate(String name, final Map props) {
        return new ReferenceExpression(name).evaluate(new ExpressionContext() {
            public Object get(String key) {
                return props.get(key);
            }

            public void put(String key, Object value) {
                props.put(key, value);
            }
        });
    }
}
