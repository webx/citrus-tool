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

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import com.alibaba.toolkit.util.StringUtil;

/**
 * "资源束"代表了和国家, 语言及地区相关的对象. 当你的程序需要地区相关的资源, 例如字符串, 那么你的程序就可以装入一个resource bundle,
 * 并指定你所需要的locale对象. Resource bundle会按照一定的规则取得尽可能接近需要的资源.
 * <p>
 * <code>ResourceBundle</code>是对<code>java.util.ResourceBundle</code>的扩展. 这个
 * <code>ResourceBundle</code>采用了和<code>java.util.ResourceBundle</code>
 * 相同的资源查找策略, 但使用了factory模式, 使之更容易扩展出新的资源格式.
 * </p>
 * <p>
 * 搜索按照如下顺序进行, 其中language1/country1/variant1是用户指定的locale,
 * language2/country2/variant2是系统默认的locale:
 * <ul>
 * <li>baseName + "_" + language1 + "_" + country1 + "_" + variant1</li>
 * <li>baseName + "_" + language1 + "_" + country1</li>
 * <li>baseName + "_" + language1</li>
 * <li>baseName + "_" + language2 + "_" + country2 + "_" + variant2</li>
 * <li>baseName + "_" + language2 + "_" + country2</li>
 * <li>baseName + "_" + language2</li>
 * <li>baseName</li>
 * </ul>
 * 另外, 搜索时忽略最后一个locale元素为空的情况, 例如: 搜索new Locale("langauge1", "", ""),
 * 则baseName_language1_country1_variant1以及baseName_language1_country1被忽略.
 * 如果全部locale元素均为空, 则只搜索baseName.
 * </p>
 *
 * @author Michael Zhou
 * @version $Id: ResourceBundle.java,v 1.1 2003/07/03 07:26:35 baobao Exp $
 */
public abstract class ResourceBundle extends java.util.ResourceBundle {
    /** 这个bundle的基本名. */
    private String baseName;

    /** 这个bundle所代表的区域. */
    private Locale locale;

    /**
     * 取得这个bundle的基本名.
     *
     * @return 基本名
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * 取得这个bundle的真实locale, 而不是用户调用<code>getBundle</code>时提供的locale参数.
     *
     * @return 这个bundle的真实locale
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * 取得用于格式化message字符串的<code>MessageBuilder</code>.
     *
     * @param key resource bundle key
     * @return <code>MessageBuilder</code>对象
     */
    public MessageBuilder getMessageBuilder(Object key) {
        return new MessageBuilder(this, key);
    }

    /**
     * 使用<code>MessageFormat</code>格式化字符串.
     *
     * @param key    要查找的键
     * @param params 参数表
     * @return key对应的字符串
     * @throws MissingResourceException 指定resource key未找到
     */
    public final String getMessage(Object key, Object[] params) {
        return StringUtil.getMessage(this, key, params);
    }

    /**
     * 从当前resource bundle或它的一个父bundle中, 取得和指定键对应的字符串. 如果不成功, 则掷出
     * <code>MissingResourceException</code>异常.
     *
     * @param key 要查找的键
     * @return key对应的字符串
     * @throws MissingResourceException 指定resource key未找到
     */
    public final String getString(Object key) {
        return (String) getObject(key);
    }

    /**
     * 从当前resource bundle或它的一个父bundle中, 取得和指定键对应的字符串. 如果不成功, 则掷出
     * <code>MissingResourceException</code>异常.
     *
     * @param key 要查找的键
     * @return key对应的字符串
     * @throws MissingResourceException 指定resource key未找到
     */
    public final String[] getStringArray(Object key) {
        return (String[]) getObject(key);
    }

    /**
     * 从当前resource bundle或它的一个父bundle中, 取得和指定键对应的<code>Map</code>. 如果不成功, 则掷出
     * <code>MissingResourceException</code>异常.
     *
     * @param key 要查找的键
     * @return key对应的<code>Map</code>
     * @throws MissingResourceException 指定resource key未找到
     */
    public final Map getMap(Object key) {
        return (Map) getObject(key);
    }

    /**
     * 从当前resource bundle或它的一个父bundle中, 取得和指定键对应的<code>List</code>. 如果不成功, 则掷出
     * <code>MissingResourceException</code>异常.
     *
     * @param key 要查找的键
     * @return key对应的<code>List</code>
     * @throws MissingResourceException 指定resource key未找到
     */
    public final List getList(Object key) {
        return (List) getObject(key);
    }

    /**
     * 从当前resource bundle或它的一个父bundle中, 取得和指定键对应的对象. 如果不成功, 则掷出
     * <code>MissingResourceException</code>异常.
     *
     * @param key 要查找的键
     * @return key对应的对象
     * @throws MissingResourceException 指定resource key未找到
     */
    public final Object getObject(Object key) {
        return super.getObject(key.toString());
    }

    /**
     * 设置这个bundle的基本名.
     *
     * @param baseName 基本名
     */
    protected final void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * 设置bundle的locale. 如果用户查找fr_FR, 但找到的是en_US,
     * 则这个bundle的locale将被设置成en_US而不是fr_FR.
     *
     * @param baseName   bundle基本名称
     * @param bundleName bundle的名称, 包括locale的扩展
     */
    protected final void setLocale(String baseName, String bundleName) {
        if (baseName.length() == bundleName.length()) {
            locale = new Locale("", "");
        } else if (baseName.length() < bundleName.length()) {
            int pos = baseName.length();
            String temp = bundleName.substring(pos + 1);

            pos = temp.indexOf('_');

            if (pos == -1) {
                locale = new Locale(temp, "", "");
                return;
            }

            String language = temp.substring(0, pos);

            temp = temp.substring(pos + 1);
            pos = temp.indexOf('_');

            if (pos == -1) {
                locale = new Locale(language, temp, "");
                return;
            }

            String country = temp.substring(0, pos);

            temp = temp.substring(pos + 1);

            locale = new Locale(language, country, temp);
        } else {
            // 基本名长于bundle名
            throw new IllegalArgumentException(MessageFormat.format(
                    ResourceBundleConstant.RB_BASE_NAME_LONGER_THAN_BUNDLE_NAME, new Object[] { baseName, bundleName }));
        }
    }

    /**
     * 设置父bundle. 如果在当前bundle中找不到指定的对象, 就会到父bundle中去搜索.
     *
     * @param parent 父bundle
     */
    protected final void setParent(ResourceBundle parent) {
        this.parent = parent;
    }

    /**
     * 取得父bundle.
     *
     * @return 父bundle对象, 如果不存在, 则返回<code>null</code>
     */
    protected final java.util.ResourceBundle getParent() {
        return parent;
    }
}
