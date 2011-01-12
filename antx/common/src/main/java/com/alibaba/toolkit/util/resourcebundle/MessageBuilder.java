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

import com.alibaba.toolkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 通过资源束创建消息的工具类, 支持所有原子类型, 方便使用.
 *
 * <p>
 * 使用方法:
 * <pre>
 *   String message = new MessageBuilder(bundle, key)
 *                   .append(param1)
 *                   .append(param2)
 *                   .toString();
 * </pre>
 * </p>
 *
 * <p>
 * 在构造此类时, 可以提供一个<code>quiet</code>参数.  如果此参数为<code>true</code>, 并且resource bundle找不到,
 * 则不会抛出<code>MissingResourceException</code>, 而是返回一个默认的字符串.
 * </p>
 *
 * @version $Id: MessageBuilder.java,v 1.1 2003/07/03 07:26:35 baobao Exp $
 * @author Michael Zhou
 */
public class MessageBuilder {
    protected final List           params = new ArrayList(5);
    protected final ResourceBundle bundle;
    protected final Object         key;

    /**
     * 创建一个<code>MessageBuilder</code>.
     *
     * @param bundleName  资源束
     * @param key         键值
     *
     * @throws MissingResourceException  指定bundle未找到, 或创建bundle错误
     */
    public MessageBuilder(String bundleName, Object key) {
        this(ResourceBundleFactory.getBundle(bundleName), key);
    }

    /**
     * 创建一个<code>MessageBuilder</code>.
     *
     * @param bundle  资源束
     * @param key     键值
     */
    public MessageBuilder(ResourceBundle bundle, Object key) {
        this.bundle = bundle;
        this.key    = key;
    }

    /**
     * 增加一个参数.
     *
     * @param param 参数
     *
     * @return <code>MessageBuilder</code>自身
     */
    public MessageBuilder append(Object param) {
        params.add(param);
        return this;
    }

    /**
     * 增加一个参数.
     *
     * @param param 参数
     *
     * @return <code>MessageBuilder</code>自身
     */
    public MessageBuilder append(boolean param) {
        params.add(new Boolean(param));
        return this;
    }

    /**
     * 增加一个参数.
     *
     * @param param 参数
     *
     * @return <code>MessageBuilder</code>自身
     */
    public MessageBuilder append(char param) {
        params.add(new Character(param));
        return this;
    }

    /**
     * 增加一个参数.
     *
     * @param param 参数
     *
     * @return <code>MessageBuilder</code>自身
     */
    public MessageBuilder append(double param) {
        params.add(new Double(param));
        return this;
    }

    /**
     * 增加一个参数.
     *
     * @param param 参数
     *
     * @return <code>MessageBuilder</code>自身
     */
    public MessageBuilder append(float param) {
        params.add(new Float(param));
        return this;
    }

    /**
     * 增加一个参数.
     *
     * @param param 参数
     *
     * @return <code>MessageBuilder</code>自身
     */
    public MessageBuilder append(int param) {
        params.add(new Integer(param));
        return this;
    }

    /**
     * 增加一个参数.
     *
     * @param param 参数
     *
     * @return <code>MessageBuilder</code>自身
     */
    public MessageBuilder append(long param) {
        params.add(new Long(param));
        return this;
    }

    /**
     * 增加多个参数.
     *
     * @param params 参数表
     *
     * @return <code>MessageBuilder</code>自身
     */
    public MessageBuilder append(Object[] params) {
        if (params != null) {
            this.params.addAll(Arrays.asList(params));
        }

        return this;
    }

    /**
     * 取得消息字符串.
     *
     * @return 消息字符串
     */
    public String toString() {
        return getMessage();
    }

    /**
     * 从资源束中取得消息字符串.
     *
     * @return 消息字符串
     *
     * @throws MissingResourceException  指定resource key未找到
     */
    protected String getMessage() {
        return StringUtil.getMessage(bundle, key, params.toArray());
    }
}
